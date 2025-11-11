package com.restaurant.billing.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.restaurant.billing.dto.billing.*;
import com.restaurant.billing.entity.*;
import com.restaurant.billing.exception.ResourceNotFoundException;
import com.restaurant.billing.repository.*;
import com.restaurant.billing.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

    private final BillRepository billRepository;
    private final OrderRepository orderRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    @Value("${app.storage.local.invoices}")
    private String invoicesPath;

    @Transactional
    public BillDto generateBill(GenerateBillRequest request) {
        UUID tenantId = TenantContext.getTenantId();

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        // Check if bill already exists
        Bill existingBill = billRepository.findByOrderId(order.getId()).orElse(null);
        if (existingBill != null) {
            return BillDto.fromEntity(existingBill);
        }

        Bill bill = Bill.builder()
                .tenantId(tenantId)
                .order(order)
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .serviceCharge(order.getServiceCharge())
                .discountAmount(request.getDiscountAmount() != null ?
                        request.getDiscountAmount() : BigDecimal.ZERO)
                .totalAmount(order.getTotalAmount().subtract(
                        request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO))
                .paymentMethod(Bill.PaymentMethod.valueOf(request.getPaymentMethod()))
                .paymentStatus(Bill.PaymentStatus.PENDING)
                .build();

        Bill saved = billRepository.save(bill);

        // Update order status
        order.setOrderStatus(Order.OrderStatus.COMPLETED);
        orderRepository.save(order);

        return BillDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public BillDto getBillById(UUID id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        return BillDto.fromEntity(bill);
    }

    @Transactional
    public BillDto recordPayment(UUID billId, PaymentRequest request) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        bill.setPaidAmount(request.getAmount());
        bill.setChangeAmount(request.getAmount().subtract(bill.getTotalAmount()));
        bill.setPaymentStatus(Bill.PaymentStatus.PAID);
        bill.setPaymentMethod(Bill.PaymentMethod.valueOf(request.getPaymentMethod()));

        Bill updated = billRepository.save(bill);
        return BillDto.fromEntity(updated);
    }

    public Resource generateInvoicePdf(UUID billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        try {
            File directory = new File(invoicesPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = "invoice-" + bill.getBillNumber() + ".pdf";
            String filePath = invoicesPath + "/" + fileName;

            PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add header
            Tenant tenant = bill.getOrder().getTenant();
            document.add(new Paragraph(tenant.getRestaurantName())
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("INVOICE")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Bill No: " + bill.getBillNumber()));
            document.add(new Paragraph("Date: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))));
            document.add(new Paragraph("Order No: " + bill.getOrder().getOrderNumber()));
            document.add(new Paragraph("\n"));

            // Add order items table
            Table table = new Table(new float[]{4, 1, 2, 2});
            table.addHeaderCell("Item");
            table.addHeaderCell("Qty");
            table.addHeaderCell("Price");
            table.addHeaderCell("Total");

            for (OrderItem item : bill.getOrder().getItems()) {
                table.addCell(item.getItemName());
                table.addCell(String.valueOf(item.getQuantity()));
                table.addCell("₹" + item.getUnitPrice());
                table.addCell("₹" + item.getTotalPrice());
            }

            document.add(table);
            document.add(new Paragraph("\n"));

            // Add totals
            document.add(new Paragraph("Subtotal: ₹" + bill.getSubtotal())
                    .setTextAlignment(TextAlignment.RIGHT));
            document.add(new Paragraph("Tax: ₹" + bill.getTaxAmount())
                    .setTextAlignment(TextAlignment.RIGHT));
            document.add(new Paragraph("Service Charge: ₹" + bill.getServiceCharge())
                    .setTextAlignment(TextAlignment.RIGHT));

            if (bill.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                document.add(new Paragraph("Discount: -₹" + bill.getDiscountAmount())
                        .setTextAlignment(TextAlignment.RIGHT));
            }

            document.add(new Paragraph("Total: ₹" + bill.getTotalAmount())
                    .setBold()
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.RIGHT));

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Payment Method: " + bill.getPaymentMethod())
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("\nThank you for your visit!")
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();

            // Update bill with invoice path
            bill.setInvoicePath(filePath);
            billRepository.save(bill);

            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }

        } catch (Exception e) {
            log.error("Error generating PDF: ", e);
            throw new RuntimeException("Error generating invoice PDF", e);
        }
    }
}
