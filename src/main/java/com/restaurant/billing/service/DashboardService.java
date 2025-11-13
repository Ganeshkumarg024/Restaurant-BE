package com.restaurant.billing.service;

import com.restaurant.billing.entity.Bill;
import com.restaurant.billing.entity.Order;
import com.restaurant.billing.entity.RestaurantTable;
import com.restaurant.billing.repository.BillRepository;
import com.restaurant.billing.repository.OrderRepository;
import com.restaurant.billing.repository.RestaurantTableRepository;
import com.restaurant.billing.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BillRepository billRepository;
    private final OrderRepository orderRepository;
    private final RestaurantTableRepository tableRepository;

    public Map<String, Object> getDashboardStats() {
        UUID tenantId = TenantContext.getTenantId();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        // Today's sales
        BigDecimal todaySales = billRepository.getDailySales(tenantId, todayStart, todayEnd)
                .stream()
                .map(row -> (BigDecimal) row[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Active orders (pending, confirmed, preparing)
        long activeOrders = orderRepository.findByTenantIdAndIsDeleted(tenantId, false)
                .stream()
                .filter(order -> order.getOrderStatus() == Order.OrderStatus.PENDING ||
                               order.getOrderStatus() == Order.OrderStatus.CONFIRMED ||
                               order.getOrderStatus() == Order.OrderStatus.PREPARING)
                .count();

        // Pending payments
        long pendingPayments = billRepository.findByTenantId(tenantId)
                .stream()
                .filter(bill -> bill.getPaymentStatus() == Bill.PaymentStatus.PENDING)
                .count();

        // Tables occupied
        long tablesOccupied = tableRepository.findAll()
                .stream()
                .filter(table -> table.getTenantId() != null && table.getTenantId().equals(tenantId))
                .filter(table -> table.getStatus() == RestaurantTable.TableStatus.OCCUPIED)
                .count();

        return Map.of(
                "todaySales", todaySales,
                "activeOrders", activeOrders,
                "pendingPayments", pendingPayments,
                "tablesOccupied", tablesOccupied
        );
    }

    public Map<String, Object> getTodaySales() {
        UUID tenantId = TenantContext.getTenantId();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal todaySales = billRepository.getDailySales(tenantId, todayStart, todayEnd)
                .stream()
                .map(row -> (BigDecimal) (row[1] != null ? row[1] : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of("amount", todaySales);
    }

    public Map<String, Object> getActiveOrders() {
        UUID tenantId = TenantContext.getTenantId();

        long activeOrders = orderRepository.findByTenantIdAndIsDeleted(tenantId, false)
                .stream()
                .filter(order -> order.getOrderStatus() == Order.OrderStatus.PENDING ||
                               order.getOrderStatus() == Order.OrderStatus.CONFIRMED ||
                               order.getOrderStatus() == Order.OrderStatus.PREPARING)
                .count();

        return Map.of("count", activeOrders);
    }

    public Map<String, Object> getPendingPayments() {
        UUID tenantId = TenantContext.getTenantId();

        long pendingPayments = billRepository.findByTenantId(tenantId)
                .stream()
                .filter(bill -> bill.getPaymentStatus() == Bill.PaymentStatus.PENDING)
                .count();

        return Map.of("count", pendingPayments);
    }

    public Map<String, Object> getTablesOccupied() {
        UUID tenantId = TenantContext.getTenantId();

        long tablesOccupied = tableRepository.findAll()
                .stream()
                .filter(table -> table.getTenantId() != null && table.getTenantId().equals(tenantId))
                .filter(table -> table.getStatus() == RestaurantTable.TableStatus.OCCUPIED)
                .count();

        return Map.of("count", tablesOccupied);
    }
}