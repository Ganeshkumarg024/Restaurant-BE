package com.restaurant.billing.dto.auth;

import lombok.*;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UserDto user;
    private TenantDto tenant;
}
