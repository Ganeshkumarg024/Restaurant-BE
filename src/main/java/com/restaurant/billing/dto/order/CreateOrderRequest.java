package com.restaurant.billing.dto.order;

import com.restaurant.billing.entity.Order;
import com.restaurant.billing.entity.OrderItem;
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
public class CreateOrderRequest {
    private UUID tableId;
    private String customerName;
    private String customerPhone;
    private String orderType;
    private List<OrderItemRequest> items;
    private String notes;
    private String deviceId;
}

