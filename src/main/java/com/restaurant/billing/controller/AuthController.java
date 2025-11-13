package com.restaurant.billing.controller;

import com.restaurant.billing.dto.auth.AuthResponse;
import com.restaurant.billing.dto.auth.CreateUserRequest;
import com.restaurant.billing.dto.auth.LoginRequest;
import com.restaurant.billing.dto.auth.RegisterRequest;
import com.restaurant.billing.dto.auth.RefreshTokenRequest;
import com.restaurant.billing.dto.auth.UserDto;
import com.restaurant.billing.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }


    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        // Implement based on SecurityContext
        return ResponseEntity.ok().build();
    }
}