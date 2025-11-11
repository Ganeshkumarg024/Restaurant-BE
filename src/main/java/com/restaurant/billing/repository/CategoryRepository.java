package com.restaurant.billing.repository;

import com.restaurant.billing.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByTenantIdAndIsActiveOrderByDisplayOrder(UUID tenantId, Boolean isActive);

    List<Category> findByTenantIdOrderByDisplayOrder(UUID tenantId);

    @Query("SELECT MAX(c.displayOrder) FROM Category c WHERE c.tenantId = :tenantId")
    Optional<Integer> findMaxDisplayOrderByTenantId(UUID tenantId);
}
