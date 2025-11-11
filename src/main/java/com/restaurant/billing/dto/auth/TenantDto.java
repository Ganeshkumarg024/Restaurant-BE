package com.restaurant.billing.dto.auth;

import com.restaurant.billing.entity.Tenant;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDto {
    private UUID id;
    private String restaurantName;
    private String restaurantCode;
    private String ownerEmail;
    private String ownerName;
    private String address;
    private String city;
    private String subscriptionPlan;
    private String subscriptionStatus;
    private LocalDateTime trialEndDate;

    public static TenantDto fromEntity(Tenant tenant) {
        return TenantDto.builder()
                .id(tenant.getId())
                .restaurantName(tenant.getRestaurantName())
                .restaurantCode(tenant.getRestaurantCode())
                .ownerEmail(tenant.getOwnerEmail())
                .ownerName(tenant.getOwnerName())
                .address(tenant.getAddress())
                .city(tenant.getCity())
                .subscriptionPlan(tenant.getSubscriptionPlan().name())
                .subscriptionStatus(tenant.getSubscriptionStatus().name())
                .trialEndDate(tenant.getTrialEndDate())
                .build();
    }
}