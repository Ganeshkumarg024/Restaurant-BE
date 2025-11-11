package com.restaurant.billing.dto.subscription;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionRequest {
    private String planName;
    private BigDecimal amount;
}
