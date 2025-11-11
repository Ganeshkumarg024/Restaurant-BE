package com.restaurant.billing.dto.billing;

import com.restaurant.billing.entity.Bill;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillDto {
    private UUID id;
    private String billNumber;
    private UUID orderId;
    private String orderNumber;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal serviceCharge;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime createdAt;

    public static BillDto fromEntity(Bill bill) {
        return BillDto.builder()
                .id(bill.getId())
                .billNumber(bill.getBillNumber())
                .orderId(bill.getOrder().getId())
                .orderNumber(bill.getOrder().getOrderNumber())
                .subtotal(bill.getSubtotal())
                .taxAmount(bill.getTaxAmount())
                .serviceCharge(bill.getServiceCharge())
                .discountAmount(bill.getDiscountAmount())
                .totalAmount(bill.getTotalAmount())
                .paymentMethod(bill.getPaymentMethod() != null ?
                        bill.getPaymentMethod().name() : null)
                .paymentStatus(bill.getPaymentStatus().name())
                .createdAt(bill.getCreatedAt())
                .build();
    }
}

