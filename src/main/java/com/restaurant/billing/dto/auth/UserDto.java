package com.restaurant.billing.dto.auth;

import com.restaurant.billing.entity.User;
import lombok.*;

import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String email;
    private String name;
    private String phone;
    private String role;
    private UUID tenantId;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .tenantId(user.getTenant().getId())
                .build();
    }
}

