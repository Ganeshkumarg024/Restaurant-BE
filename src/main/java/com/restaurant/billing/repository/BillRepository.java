package com.restaurant.billing.repository;

import com.restaurant.billing.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BillRepository extends JpaRepository<Bill, UUID> {
    Optional<Bill> findByOrderId(UUID orderId);
    Optional<Bill> findByBillNumber(String billNumber);
    List<Bill> findByTenantId(UUID tenantId);

    @Query("SELECT b FROM Bill b WHERE b.tenantId = :tenantId " +
            "AND b.createdAt BETWEEN :startDate AND :endDate")
    List<Bill> findByDateRange(UUID tenantId, LocalDateTime startDate, LocalDateTime endDate);
}
