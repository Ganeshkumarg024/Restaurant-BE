package com.restaurant.billing.controller;

import com.restaurant.billing.dto.menu.*;
import com.restaurant.billing.security.TenantContext;
import com.restaurant.billing.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<List<MenuItemDto>> getAllMenuItems() {
        UUID tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(menuService.getAllMenuItems(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemDto> getMenuItemById(@PathVariable UUID id) {
        return ResponseEntity.ok(menuService.getMenuItemById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<MenuItemDto> createMenuItem(
            @RequestBody MenuItemDto dto,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(menuService.createMenuItem(dto, deviceId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<MenuItemDto> updateMenuItem(
            @PathVariable UUID id,
            @RequestBody MenuItemDto dto,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        return ResponseEntity.ok(menuService.updateMenuItem(id, dto, deviceId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable UUID id) {
        menuService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        UUID tenantId = TenantContext.getTenantId();
        return ResponseEntity.ok(menuService.getAllCategories(tenantId));
    }
}
