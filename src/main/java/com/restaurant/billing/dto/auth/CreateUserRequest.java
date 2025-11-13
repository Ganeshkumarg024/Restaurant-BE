package com.restaurant.billing.dto.auth;

import com.restaurant.billing.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    private String email;
    private String name;
    private String phone;
    private String password;
    private User.UserRole role;
}