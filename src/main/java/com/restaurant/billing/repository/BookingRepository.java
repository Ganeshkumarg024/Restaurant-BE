package com.restaurant.billing.repository;

import com.restaurant.billing.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByTenantId(UUID tenantId);

    @Query("SELECT b FROM Booking b WHERE b.tenantId = :tenantId " +
            "AND b.bookingDate BETWEEN :startDate AND :endDate")
    List<Booking> findByDateRange(UUID tenantId, LocalDateTime startDate, LocalDateTime endDate);
}