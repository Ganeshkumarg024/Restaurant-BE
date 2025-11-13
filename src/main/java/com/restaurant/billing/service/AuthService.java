package com.restaurant.billing.service;

import com.restaurant.billing.dto.auth.AuthResponse;
import com.restaurant.billing.dto.auth.CreateUserRequest;
import com.restaurant.billing.dto.auth.LoginRequest;
import com.restaurant.billing.dto.auth.RegisterRequest;
import com.restaurant.billing.dto.auth.RefreshTokenRequest;
import com.restaurant.billing.dto.auth.TenantDto;
import com.restaurant.billing.dto.auth.UserDto;
import com.restaurant.billing.security.TenantContext;
import com.restaurant.billing.entity.Tenant;
import com.restaurant.billing.entity.User;
import com.restaurant.billing.exception.BadRequestException;
import com.restaurant.billing.exception.ResourceNotFoundException;
import com.restaurant.billing.repository.TenantRepository;
import com.restaurant.billing.repository.UserRepository;
import com.restaurant.billing.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;


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

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (tenantRepository.existsByOwnerEmail(request.getOwnerEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // Create tenant
        Tenant tenant = Tenant.builder()
                .restaurantName(request.getRestaurantName())
                .ownerEmail(request.getOwnerEmail())
                .ownerName(request.getOwnerName())
                .ownerPhone(request.getOwnerPhone())
                .subscriptionPlan(Tenant.SubscriptionPlan.TRIAL)
                .subscriptionStatus(Tenant.SubscriptionStatus.TRIAL)
                .trialEndDate(LocalDateTime.now().plusDays(7))
                .isActive(true)
                .maxUsers(5)
                .maxStorageGb(1)
                .currency("INR")
                .timezone("Asia/Kolkata")
                .build();

        tenant = tenantRepository.save(tenant);

        // Create admin user
        User user = User.builder()
                .tenant(tenant)
                .email(request.getOwnerEmail())
                .name(request.getOwnerName())
                .phone(request.getOwnerPhone())
                .role(User.UserRole.OWNER)
                .authProvider(User.AuthProvider.LOCAL)
                .isActive(true)
                .lastLogin(LocalDateTime.now())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);

        // Initialize default features
        featureService.initializeDefaultFeatures(tenant.getId());

        log.info("New tenant registered: {}", tenant.getId());

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
    public AuthResponse login(LoginRequest request) {
        // Find tenant by restaurant code
        Tenant tenant = tenantRepository.findByRestaurantCode(request.getRestaurantCode())
                .orElseThrow(() -> new BadRequestException("Invalid restaurant code"));

        if (!tenant.getIsActive()) {
            throw new BadRequestException("Restaurant account is not active");
        }

        // Find user by email (username) and tenant
        User user = userRepository.findByEmailAndTenantId(request.getUsername(), tenant.getId())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (!user.getIsActive()) {
            throw new BadRequestException("User account is not active");
        }

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid credentials");
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

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
    public UserDto createUser(CreateUserRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        // Check if tenant has Prime plan
        if (tenant.getSubscriptionPlan() != Tenant.SubscriptionPlan.PRIME) {
            throw new BadRequestException("Only Prime plan allows multiple users");
        }

        // Check user count (admin + sub-users <= 3 total, since admin can create up to 2 sub-users)
        long userCount = userRepository.findByTenantIdAndIsActive(tenantId, true).size();
        if (userCount >= 3) {
            throw new BadRequestException("Maximum 3 users allowed for Prime plan");
        }

        // Check if email already exists for this tenant
        if (userRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
            throw new BadRequestException("Email already exists for this restaurant");
        }

        User user = User.builder()
                .tenant(tenant)
                .email(request.getEmail())
                .name(request.getName())
                .phone(request.getPhone())
                .role(request.getRole())
                .authProvider(User.AuthProvider.LOCAL)
                .isActive(true)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);

        return UserDto.fromEntity(user);
    }
}