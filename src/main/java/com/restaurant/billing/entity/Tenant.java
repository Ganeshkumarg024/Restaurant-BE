package com.restaurant.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "restaurant_name", nullable = false)
    private String restaurantName;

    @Column(name = "restaurant_code", unique = true, nullable = false)
    private String restaurantCode;

    @Column(name = "owner_email", unique = true, nullable = false)
    private String ownerEmail;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "owner_phone")
    private String ownerPhone;

    @Column(name = "logo_path")
    private String logoPath;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "pincode")
    private String pincode;

    @Column(name = "gstin")
    private String gstin;

    @Column(name = "currency", length = 10)
    private String currency = "INR";

    @Column(name = "timezone")
    private String timezone = "Asia/Kolkata";

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;

    @Column(name = "service_charge_rate", precision = 5, scale = 2)
    private BigDecimal serviceChargeRate = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan", length = 50)
    private SubscriptionPlan subscriptionPlan = SubscriptionPlan.TRIAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", length = 50)
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.TRIAL;

    @Column(name = "trial_end_date")
    private LocalDateTime trialEndDate;

    @Column(name = "subscription_start_date")
    private LocalDateTime subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private LocalDateTime subscriptionEndDate;

    @Column(name = "razorpay_subscription_id")
    private String razorpaySubscriptionId;

    @Column(name = "razorpay_customer_id")
    private String razorpayCustomerId;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "max_users")
    private Integer maxUsers = 5;

    @Column(name = "max_storage_gb")
    private Integer maxStorageGb = 1;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum SubscriptionPlan {
        TRIAL, PRIME
    }

    public enum SubscriptionStatus {
        TRIAL, ACTIVE, EXPIRED, CANCELLED, SUSPENDED
    }

    @PrePersist
    public void prePersist() {
        if (restaurantCode == null) {
            restaurantCode = generateRestaurantCode();
        }
        if (subscriptionPlan == SubscriptionPlan.TRIAL) {
            trialEndDate = LocalDateTime.now().plusDays(7);
        }
    }

    private String generateRestaurantCode() {
        return "REST" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}