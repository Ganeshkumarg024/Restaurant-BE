package com.restaurant.billing.repository;

import com.restaurant.billing.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByOwnerEmail(String ownerEmail);
    Optional<Tenant> findByRestaurantCode(String restaurantCode);
    List<Tenant> findBySubscriptionStatusAndTrialEndDateBefore(
            Tenant.SubscriptionStatus status, LocalDateTime date);
    boolean existsByOwnerEmail(String email);
}
