package com.restaurant.billing.dto.auth;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginRequest {
    private String email;
    private String name;
    private String googleId;
    private String idToken;
}