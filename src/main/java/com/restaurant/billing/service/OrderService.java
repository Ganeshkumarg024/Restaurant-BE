package com.restaurant.billing.service;

import com.restaurant.billing.dto.order.*;
import com.restaurant.billing.entity.*;
import com.restaurant.billing.exception.ResourceNotFoundException;
import com.restaurant.billing.repository.*;
import com.restaurant.billing.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantTableRepository tableRepository;
    private final TenantRepository tenantRepository;

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {
        UUID tenantId = TenantContext.getTenantId();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        RestaurantTable table = null;
        if (request.getTableId() != null) {
            table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
        }

        Order order = Order.builder()
                .tenantId(tenantId)
                .table(table)
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .orderType(Order.OrderType.valueOf(request.getOrderType()))
                .orderStatus(Order.OrderStatus.PENDING)
                .notes(request.getNotes())
                .deviceId(request.getDeviceId())
                .isDeleted(false)
                .version(1L)
                .build();

        // Add order items
        for (OrderItemRequest itemReq : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

            OrderItem orderItem = OrderItem.builder()
                    .menuItem(menuItem)
                    .itemName(menuItem.getName())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .specialInstructions(itemReq.getSpecialInstructions())
                    .status(OrderItem.ItemStatus.PENDING)
                    .build();

            order.addItem(orderItem);
        }

        // Calculate totals
        order.calculateTotals(tenant.getTaxRate(), tenant.getServiceChargeRate());

        Order saved = orderRepository.save(order);
        return OrderDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        UUID tenantId = TenantContext.getTenantId();
        return orderRepository.findByTenantIdAndIsDeleted(tenantId, false)
                .stream()
                .map(OrderDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return OrderDto.fromEntity(order);
    }

    @Transactional
    public OrderDto updateOrderStatus(UUID id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setOrderStatus(Order.OrderStatus.valueOf(status));
        order.setVersion(order.getVersion() + 1);
        order.setSyncedAt(null);

        Order updated = orderRepository.save(order);
        return OrderDto.fromEntity(updated);
    }
}