package com.restaurant.billing.repository;

import com.restaurant.billing.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {
    List<InventoryItem> findByTenantIdAndIsActive(UUID tenantId, Boolean isActive);

    @Query("SELECT i FROM InventoryItem i WHERE i.tenantId = :tenantId " +
            "AND i.currentStock <= i.minimumStock AND i.isActive = true")
    List<InventoryItem> findLowStockItems(UUID tenantId);
}
