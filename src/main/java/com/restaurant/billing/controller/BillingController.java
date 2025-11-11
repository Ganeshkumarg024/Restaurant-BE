package com.restaurant.billing.controller;

import com.restaurant.billing.dto.billing.*;
import com.restaurant.billing.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping("/generate")
    public ResponseEntity<BillDto> generateBill(@RequestBody GenerateBillRequest request) {
        return ResponseEntity.ok(billingService.generateBill(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillDto> getBillById(@PathVariable UUID id) {
        return ResponseEntity.ok(billingService.getBillById(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadInvoice(@PathVariable UUID id) {
        Resource resource = billingService.generateInvoicePdf(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"invoice-" + id + ".pdf\"")
                .body(resource);
    }

    @PostMapping("/{id}/payment")
    public ResponseEntity<BillDto> recordPayment(
            @PathVariable UUID id,
            @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(billingService.recordPayment(id, request));
    }
}