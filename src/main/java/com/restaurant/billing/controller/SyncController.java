package com.restaurant.billing.controller;

import com.restaurant.billing.dto.sync.*;
import com.restaurant.billing.security.TenantContext;
import com.restaurant.billing.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    @PostMapping
    public ResponseEntity<SyncResponse> performSync(@RequestBody SyncRequest request) {
        return ResponseEntity.ok(syncService.performSync(request));
    }

    @GetMapping("/delta")
    public ResponseEntity<DeltaSyncResponse> getDeltaChanges(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastSyncTime,
            @RequestParam String deviceId) {
        UUID tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(syncService.getDeltaChanges(tenantId, lastSyncTime, deviceId));
    }
}