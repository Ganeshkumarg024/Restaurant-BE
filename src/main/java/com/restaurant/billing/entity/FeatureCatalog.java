package com.restaurant.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "feature_catalog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FeatureCatalog {

    @Id
    @Column(name = "feature_code", length = 50)
    private String featureCode;

    @Column(name = "feature_name", nullable = false)
    private String featureName;

    @Column(name = "feature_description")
    private String featureDescription;

    @Column(name = "feature_category")
    private String featureCategory;

    @Column(name = "is_premium")
    private Boolean isPremium = false;

    @Column(name = "default_enabled")
    private Boolean defaultEnabled = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}