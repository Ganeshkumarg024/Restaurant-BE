package com.restaurant.billing.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String restaurantName;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;
    private String password;
}