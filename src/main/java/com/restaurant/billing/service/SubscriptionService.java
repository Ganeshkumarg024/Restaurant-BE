package com.restaurant.billing.service;

import com.razorpay.*;
import com.razorpay.Order;
import com.restaurant.billing.dto.subscription.*;
import com.restaurant.billing.entity.*;
import com.restaurant.billing.entity.Payment;
import com.restaurant.billing.exception.BadRequestException;
import com.restaurant.billing.exception.ResourceNotFoundException;
import com.restaurant.billing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final TenantRepository tenantRepository;
    private final PaymentRepository paymentRepository;
    private final FeatureService featureService;


    @Value("${app.razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${app.razorpay.key-secret}")
    private String razorpayKeySecret;

    @Transactional
    public SubscriptionResponse createSubscription(UUID tenantId, CreateSubscriptionRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            // Create Razorpay order
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "subscription_" + tenantId);

            Order order = razorpay.orders.create(orderRequest);

            // Save payment record
            Payment payment = Payment.builder()
                    .tenantId(tenantId)
                    .razorpayOrderId(order.get("id"))
                    .amount(request.getAmount())
                    .currency("INR")
                    .paymentType(Payment.PaymentType.SUBSCRIPTION)
                    .status(Payment.PaymentStatus.PENDING)
                    .description("PRIME Subscription - Monthly")
                    .build();

            paymentRepository.save(payment);

            return SubscriptionResponse.builder()
                    .orderId(order.get("id"))
                    .amount(request.getAmount())
                    .currency("INR")
                    .razorpayKeyId(razorpayKeyId)
                    .build();

        } catch (RazorpayException e) {
            log.error("Razorpay error: ", e);
            throw new BadRequestException("Failed to create subscription: " + e.getMessage());
        }
    }

    @Transactional
    public void verifyPayment(PaymentVerificationRequest request) {
        try {
            String generatedSignature = Utils.getHash(
                    request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId(),
                    razorpayKeySecret
            );

            if (!generatedSignature.equals(request.getRazorpaySignature())) {
                throw new BadRequestException("Invalid payment signature");
            }

            // Update payment status
            Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setRazorpaySignature(request.getRazorpaySignature());
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            paymentRepository.save(payment);

            // Activate subscription
            activateSubscription(payment.getTenantId());

        } catch (Exception e) {
            log.error("Payment verification error: ", e);
            throw new BadRequestException("Payment verification failed");
        }
    }

    @Transactional
    public void activateSubscription(UUID tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        tenant.setSubscriptionPlan(Tenant.SubscriptionPlan.PRIME);
        tenant.setSubscriptionStatus(Tenant.SubscriptionStatus.ACTIVE);
        tenant.setSubscriptionStartDate(LocalDateTime.now());
        tenant.setSubscriptionEndDate(LocalDateTime.now().plusMonths(1));
        tenant.setMaxUsers(50);
        tenant.setMaxStorageGb(50);

        tenantRepository.save(tenant);

        // Enable all premium features
        featureService.enableAllPremiumFeatures(tenantId);

        log.info("Subscription activated for tenant: {}", tenantId);
    }

    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void checkTrialExpiry() {
        List<Tenant> expiredTrials = tenantRepository
                .findBySubscriptionStatusAndTrialEndDateBefore(
                        Tenant.SubscriptionStatus.TRIAL,
                        LocalDateTime.now()
                );

        for (Tenant tenant : expiredTrials) {
            tenant.setSubscriptionStatus(Tenant.SubscriptionStatus.EXPIRED);
            tenant.setIsActive(false);
            tenantRepository.save(tenant);

            log.info("Trial expired for tenant: {}", tenant.getId());
        }
    }

    @Scheduled(cron = "0 0 3 * * ?") // Run daily at 3 AM
    @Transactional
    public void checkSubscriptionExpiry() {
        List<Tenant> tenants = tenantRepository.findAll();

        for (Tenant tenant : tenants) {
            if (tenant.getSubscriptionStatus() == Tenant.SubscriptionStatus.ACTIVE &&
                    tenant.getSubscriptionEndDate() != null &&
                    tenant.getSubscriptionEndDate().isBefore(LocalDateTime.now())) {

                tenant.setSubscriptionStatus(Tenant.SubscriptionStatus.EXPIRED);
                tenant.setIsActive(false);
                tenantRepository.save(tenant);

                log.info("Subscription expired for tenant: {}", tenant.getId());
            }
        }
    }
}