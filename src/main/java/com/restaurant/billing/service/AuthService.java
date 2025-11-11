package com.restaurant.billing.service;

import com.restaurant.billing.dto.auth.*;
import com.restaurant.billing.entity.Tenant;
import com.restaurant.billing.entity.User;
import com.restaurant.billing.exception.BadRequestException;
import com.restaurant.billing.exception.ResourceNotFoundException;
import com.restaurant.billing.repository.TenantRepository;
import com.restaurant.billing.repository.UserRepository;
import com.restaurant.billing.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final JwtUtil jwtUtil;
    private final FeatureService featureService;

    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        // In production, verify token with Google OAuth
        String email = request.getEmail();
        String name = request.getName();
        String googleId = request.getGoogleId();

        // Check if user exists
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // Create new tenant and owner user
            Tenant tenant = createNewTenant(email, name);
            user = createOwnerUser(tenant, email, name, googleId);

            // Initialize default features
            featureService.initializeDefaultFeatures(tenant.getId());

            log.info("New tenant created: {}", tenant.getId());
        } else {
            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(
                user.getId(), user.getEmail(), user.getTenant().getId(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Save refresh token
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserDto.fromEntity(user))
                .tenant(TenantDto.fromEntity(user.getTenant()))
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        UUID userId = UUID.fromString(jwtUtil.extractClaims(refreshToken).getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!refreshToken.equals(user.getRefreshToken()) ||
                user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Refresh token expired");
        }

        // Generate new tokens
        String newAccessToken = jwtUtil.generateAccessToken(
                user.getId(), user.getEmail(), user.getTenant().getId(), user.getRole().name());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Update refresh token
        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
        userRepository.save(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(UserDto.fromEntity(user))
                .tenant(TenantDto.fromEntity(user.getTenant()))
                .build();
    }

    private Tenant createNewTenant(String ownerEmail, String ownerName) {
        Tenant tenant = Tenant.builder()
                .ownerEmail(ownerEmail)
                .ownerName(ownerName)
                .restaurantName(ownerName + "'s Restaurant")
                .subscriptionPlan(Tenant.SubscriptionPlan.TRIAL)
                .subscriptionStatus(Tenant.SubscriptionStatus.TRIAL)
                .trialEndDate(LocalDateTime.now().plusDays(7))
                .isActive(true)
                .maxUsers(5)
                .maxStorageGb(1)
                .currency("INR")
                .timezone("Asia/Kolkata")
                .build();

        return tenantRepository.save(tenant);
    }

    private User createOwnerUser(Tenant tenant, String email, String name, String googleId) {
        User user = User.builder()
                .tenant(tenant)
                .email(email)
                .name(name)
                .googleId(googleId)
                .role(User.UserRole.OWNER)
                .authProvider(User.AuthProvider.GOOGLE)
                .isActive(true)
                .lastLogin(LocalDateTime.now())
                .passwordHash("PasswordHash")
                .build();

        return userRepository.save(user);
    }
}