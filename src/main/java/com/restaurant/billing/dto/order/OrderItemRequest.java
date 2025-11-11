package com.restaurant.billing.dto.order;
import lombok.*;
import java.util.UUID;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    private UUID menuItemId;
    private Integer quantity;
    private String specialInstructions;
}


