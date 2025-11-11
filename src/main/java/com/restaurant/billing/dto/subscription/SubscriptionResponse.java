package com.restaurant.billing.dto.subscription;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String razorpayKeyId;
}

