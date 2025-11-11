package com.restaurant.billing.repository;

import com.restaurant.billing.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findByTenantIdAndIsDeleted(UUID tenantId, Boolean isDeleted);
    List<MenuItem> findByTenantIdAndCategoryIdAndIsDeleted(
            UUID tenantId, UUID categoryId, Boolean isDeleted);

    @Query("SELECT m FROM MenuItem m WHERE m.tenantId = :tenantId " +
            "AND m.updatedAt > :lastSyncTime AND m.deviceId != :deviceId")
    List<MenuItem> findDeltaChanges(UUID tenantId, LocalDateTime lastSyncTime, String deviceId);
}