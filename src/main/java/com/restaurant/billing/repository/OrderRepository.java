package com.restaurant.billing.repository;

import com.restaurant.billing.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByTenantIdAndIsDeleted(UUID tenantId, Boolean isDeleted);
    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId " +
            "AND o.createdAt BETWEEN :startDate AND :endDate AND o.isDeleted = false")
    List<Order> findByDateRange(UUID tenantId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId " +
            "AND o.updatedAt > :lastSyncTime AND o.deviceId != :deviceId")
    List<Order> findDeltaChanges(UUID tenantId, LocalDateTime lastSyncTime, String deviceId);

    List<Order> findByTenantIdAndSyncedAtIsNullAndIsDeleted(
            UUID tenantId, Boolean isDeleted);
}