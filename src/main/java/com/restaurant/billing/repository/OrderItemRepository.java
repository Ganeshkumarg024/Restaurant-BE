package com.restaurant.billing.repository;

import com.restaurant.billing.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @Query("SELECT oi.menuItem.id, oi.itemName, SUM(oi.quantity), SUM(oi.totalPrice) " +
            "FROM OrderItem oi JOIN oi.order o " +
            "WHERE o.tenantId = :tenantId AND o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY oi.menuItem.id, oi.itemName ORDER BY SUM(oi.totalPrice) DESC")
    List<Object[]> getProductSales(UUID tenantId, LocalDateTime startDate, LocalDateTime endDate);
}