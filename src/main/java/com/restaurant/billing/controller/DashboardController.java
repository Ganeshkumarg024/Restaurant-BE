package com.restaurant.billing.controller;

import com.restaurant.billing.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @GetMapping("/today-sales")
    public ResponseEntity<Map<String, Object>> getTodaySales() {
        return ResponseEntity.ok(dashboardService.getTodaySales());
    }

    @GetMapping("/active-orders")
    public ResponseEntity<Map<String, Object>> getActiveOrders() {
        return ResponseEntity.ok(dashboardService.getActiveOrders());
    }

    @GetMapping("/pending-payments")
    public ResponseEntity<Map<String, Object>> getPendingPayments() {
        return ResponseEntity.ok(dashboardService.getPendingPayments());
    }

    @GetMapping("/tables-occupied")
    public ResponseEntity<Map<String, Object>> getTablesOccupied() {
        return ResponseEntity.ok(dashboardService.getTablesOccupied());
    }
}