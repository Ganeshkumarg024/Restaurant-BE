package com.restaurant.billing.service;

import com.restaurant.billing.dto.sync.*;
import com.restaurant.billing.entity.*;
import com.restaurant.billing.repository.*;
import com.restaurant.billing.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final SyncLogRepository syncLogRepository;

    @Transactional
    public SyncResponse performSync(SyncRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        List<SyncResultDto> results = new ArrayList<>();
        List<ConflictDto> conflicts = new ArrayList<>();

        // Process each sync item
        for (SyncItemDto item : request.getData()) {
            try {
                SyncResultDto result = processSyncItem(tenantId, item, request.getDeviceId());
                results.add(result);

                // Log sync
                logSync(tenantId, request.getDeviceId(), item, SyncLog.SyncStatus.SUCCESS);

            } catch (Exception e) {
                log.error("Sync error for item: {}", item.getEntityId(), e);

                ConflictDto conflict = ConflictDto.builder()
                        .entityId(item.getEntityId())
                        .entityType(item.getEntityType())
                        .reason(e.getMessage())
                        .build();
                conflicts.add(conflict);

                // Log failed sync
                logSync(tenantId, request.getDeviceId(), item, SyncLog.SyncStatus.FAILED);
            }
        }

        return SyncResponse.builder()
                .success(conflicts.isEmpty())
                .results(results)
                .conflicts(conflicts)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public DeltaSyncResponse getDeltaChanges(UUID tenantId, LocalDateTime lastSyncTime, String deviceId) {
        List<ChangeDto> changes = new ArrayList<>();

        // Get order changes
        List<Order> orderChanges = orderRepository.findDeltaChanges(tenantId, lastSyncTime, deviceId);
        changes.addAll(orderChanges.stream()
                .map(order -> ChangeDto.builder()
                        .entityType("ORDER")
                        .entityId(order.getId())
                        .operation(order.getIsDeleted() ? "DELETE" : "UPDATE")
                        .timestamp(order.getUpdatedAt())
                        .version(order.getVersion())
                        .payload(convertOrderToMap(order))
                        .build())
                .collect(Collectors.toList()));

        // Get menu item changes
        List<MenuItem> menuChanges = menuItemRepository.findDeltaChanges(tenantId, lastSyncTime, deviceId);
        changes.addAll(menuChanges.stream()
                .map(item -> ChangeDto.builder()
                        .entityType("MENU_ITEM")
                        .entityId(item.getId())
                        .operation(item.getIsDeleted() ? "DELETE" : "UPDATE")
                        .timestamp(item.getUpdatedAt())
                        .version(item.getVersion())
                        .payload(convertMenuItemToMap(item))
                        .build())
                .collect(Collectors.toList()));

        return DeltaSyncResponse.builder()
                .changes(changes)
                .timestamp(LocalDateTime.now())
                .hasMore(false)
                .build();
    }

    private SyncResultDto processSyncItem(UUID tenantId, SyncItemDto item, String deviceId) {
        switch (item.getEntityType()) {
            case "ORDER":
                return processOrderSync(tenantId, item, deviceId);
            case "MENU_ITEM":
                return processMenuItemSync(tenantId, item, deviceId);
            default:
                throw new IllegalArgumentException("Unknown entity type: " + item.getEntityType());
        }
    }

    private SyncResultDto processOrderSync(UUID tenantId, SyncItemDto item, String deviceId) {
        Order order = orderRepository.findById(item.getEntityId()).orElse(null);

        if (order == null && item.getOperation().equals("CREATE")) {
            // Create new order
            // Implementation depends on payload structure
            return SyncResultDto.builder()
                    .entityId(item.getEntityId())
                    .status("SUCCESS")
                    .serverVersion(1L)
                    .build();
        } else if (order != null && item.getOperation().equals("UPDATE")) {
            // Check version conflict
            if (item.getClientVersion() < order.getVersion()) {
                throw new RuntimeException("Version conflict detected");
            }

            // Update order
            order.setSyncedAt(LocalDateTime.now());
            order.setVersion(order.getVersion() + 1);
            orderRepository.save(order);

            return SyncResultDto.builder()
                    .entityId(order.getId())
                    .status("SUCCESS")
                    .serverVersion(order.getVersion())
                    .build();
        }

        throw new RuntimeException("Invalid sync operation");
    }

    private SyncResultDto processMenuItemSync(UUID tenantId, SyncItemDto item, String deviceId) {
        // Similar to processOrderSync
        return SyncResultDto.builder()
                .entityId(item.getEntityId())
                .status("SUCCESS")
                .serverVersion(1L)
                .build();
    }

    private void logSync(UUID tenantId, String deviceId, SyncItemDto item, SyncLog.SyncStatus status) {
        SyncLog log = SyncLog.builder()
                .tenantId(tenantId)
                .deviceId(deviceId)
                .entityType(item.getEntityType())
                .entityId(item.getEntityId())
                .operation(SyncLog.SyncOperation.valueOf(item.getOperation()))
                .status(status)
                .clientVersion(item.getClientVersion())
                .build();

        syncLogRepository.save(log);
    }

    private Map<String, Object> convertOrderToMap(Order order) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("orderNumber", order.getOrderNumber());
        map.put("totalAmount", order.getTotalAmount());
        map.put("status", order.getOrderStatus().name());
        // Add other fields as needed
        return map;
    }

    private Map<String, Object> convertMenuItemToMap(MenuItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", item.getId());
        map.put("name", item.getName());
        map.put("price", item.getPrice());
        map.put("isAvailable", item.getIsAvailable());
        // Add other fields as needed
        return map;
    }
}
