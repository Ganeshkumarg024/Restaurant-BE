package com.restaurant.billing.controller;

import com.restaurant.billing.dto.subscription.*;
import com.restaurant.billing.security.TenantContext;
import com.restaurant.billing.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/create")
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @RequestBody CreateSubscriptionRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(subscriptionService.createSubscription(tenantId, request));
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        subscriptionService.verifyPayment(request);
        return ResponseEntity.ok().build();
    }
}
