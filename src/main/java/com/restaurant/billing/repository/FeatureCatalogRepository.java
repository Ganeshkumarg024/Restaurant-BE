package com.restaurant.billing.repository;

import com.restaurant.billing.entity.FeatureCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeatureCatalogRepository extends JpaRepository<FeatureCatalog, String> {
    List<FeatureCatalog> findByIsPremium(Boolean isPremium);
}