package com.restaurant.billing.controller;

import com.restaurant.billing.dto.auth.CreateUserRequest;
import com.restaurant.billing.dto.auth.UserDto;
import com.restaurant.billing.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(authService.createUser(request));
    }
}