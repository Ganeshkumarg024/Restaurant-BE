package com.restaurant.billing.dto.billing;

import com.restaurant.billing.entity.Bill;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateBillRequest {
    private UUID orderId;
    private String paymentMethod;
    private BigDecimal discountAmount;
}

