package com.restaurant.billing.service;

import com.restaurant.billing.entity.*;
import com.restaurant.billing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeatureService {

    private final FeatureCatalogRepository featureCatalogRepository;
    private final TenantFeatureRepository tenantFeatureRepository;

    @Transactional
    public void initializeDefaultFeatures(UUID tenantId) {
        // Core features for trial users
        List<String> coreFeatures = Arrays.asList(
                "QR_ORDERING",
                "TABLE_BOOKING",
                "SPLIT_BILLING",
                "STAFF_MANAGEMENT"
        );

        for (String featureCode : coreFeatures) {
            FeatureCatalog feature = featureCatalogRepository.findById(featureCode)
                    .orElse(createDefaultFeature(featureCode));

            TenantFeature tenantFeature = TenantFeature.builder()
                    .tenantId(tenantId)
                    .feature(feature)
                    .isEnabled(true)
                    .enabledAt(LocalDateTime.now())
                    .build();

            tenantFeatureRepository.save(tenantFeature);
        }

        log.info("Initialized default features for tenant: {}", tenantId);
    }

    @Transactional
    public void enableAllPremiumFeatures(UUID tenantId) {
        List<String> premiumFeatures = Arrays.asList(
                "INVENTORY_MANAGEMENT",
                "KITCHEN_DISPLAY",
                "ADVANCED_REPORTS"
        );

        for (String featureCode : premiumFeatures) {
            FeatureCatalog feature = featureCatalogRepository.findById(featureCode)
                    .orElse(createDefaultFeature(featureCode));

            TenantFeature existingFeature = tenantFeatureRepository
                    .findByTenantIdAndFeatureFeatureCode(tenantId, featureCode)
                    .orElse(null);

            if (existingFeature == null) {
                TenantFeature tenantFeature = TenantFeature.builder()
                        .tenantId(tenantId)
                        .feature(feature)
                        .isEnabled(true)
                        .enabledAt(LocalDateTime.now())
                        .build();

                tenantFeatureRepository.save(tenantFeature);
            } else {
                existingFeature.setIsEnabled(true);
                existingFeature.setEnabledAt(LocalDateTime.now());
                tenantFeatureRepository.save(existingFeature);
            }
        }

        log.info("Enabled premium features for tenant: {}", tenantId);
    }

    public boolean isFeatureEnabled(UUID tenantId, String featureCode) {
        return tenantFeatureRepository
                .findByTenantIdAndFeatureFeatureCode(tenantId, featureCode)
                .map(TenantFeature::getIsEnabled)
                .orElse(false);
    }

    private FeatureCatalog createDefaultFeature(String featureCode) {
        FeatureCatalog feature = FeatureCatalog.builder()
                .featureCode(featureCode)
                .featureName(featureCode.replace("_", " "))
                .featureCategory("CORE")
                .isPremium(false)
                .defaultEnabled(true)
                .build();

        return featureCatalogRepository.save(feature);
    }
}