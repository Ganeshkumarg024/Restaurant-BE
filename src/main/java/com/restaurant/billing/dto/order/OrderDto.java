package com.restaurant.billing.dto.order;

import com.restaurant.billing.entity.Order;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private UUID id;
    private String orderNumber;
    private UUID tableId;
    private String tableName;
    private String customerName;
    private String customerPhone;
    private String orderStatus;
    private String orderType;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal serviceCharge;
    private BigDecimal totalAmount;
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;

    public static OrderDto fromEntity(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .tableId(order.getTable() != null ? order.getTable().getId() : null)
                .tableName(order.getTable() != null ? order.getTable().getTableNumber() : null)
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .orderStatus(order.getOrderStatus().name())
                .orderType(order.getOrderType().name())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .serviceCharge(order.getServiceCharge())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream()
                        .map(OrderItemDto::fromEntity)
                        .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .build();
    }
}
