package com.restaurant.billing.repository;

import com.restaurant.billing.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, UUID> {
    List<RestaurantTable> findByTenantIdAndIsActive(UUID tenantId, Boolean isActive);
    Optional<RestaurantTable> findByTenantIdAndTableNumber(UUID tenantId, String tableNumber);
}