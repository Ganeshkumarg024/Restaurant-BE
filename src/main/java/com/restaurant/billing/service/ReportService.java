package com.restaurant.billing.service;

import com.restaurant.billing.repository.BillRepository;
import com.restaurant.billing.repository.OrderItemRepository;
import com.restaurant.billing.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BillRepository billRepository;
    private final OrderItemRepository orderItemRepository;

    public List<Map<String, Object>> getProductWiseSales(LocalDateTime startDate, LocalDateTime endDate) {
        UUID tenantId = TenantContext.getTenantId();
        List<Object[]> results = orderItemRepository.getProductSales(tenantId, startDate, endDate);

        return results.stream().map(row -> {
            Object menuItemId = row[0];
            Object itemName = row[1];
            Object totalQuantity = row[2] != null ? row[2] : 0L;
            Object totalRevenue = row[3] != null ? row[3] : BigDecimal.ZERO;
            return Map.of(
                    "menuItemId", menuItemId,
                    "itemName", itemName,
                    "totalQuantity", totalQuantity,
                    "totalRevenue", totalRevenue
            );
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getDayWiseSales(LocalDateTime startDate, LocalDateTime endDate) {
        UUID tenantId = TenantContext.getTenantId();
        List<Object[]> results = billRepository.getDailySales(tenantId, startDate, endDate);

        return results.stream().map(row -> {
            Object date = row[0];
            Object totalSales = row[1] != null ? row[1] : BigDecimal.ZERO;
            return Map.of(
                    "date", date,
                    "totalSales", totalSales
            );
        }).collect(Collectors.toList());
    }
}