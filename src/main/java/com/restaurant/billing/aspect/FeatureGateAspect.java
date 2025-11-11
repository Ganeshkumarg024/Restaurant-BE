package com.restaurant.billing.aspect;

import com.restaurant.billing.annotation.FeatureGate;
import com.restaurant.billing.exception.FeatureNotEnabledException;
import com.restaurant.billing.security.TenantContext;
import com.restaurant.billing.service.FeatureService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class FeatureGateAspect {

    private final FeatureService featureService;

    @Around("@annotation(featureGate)")
    public Object checkFeatureAccess(ProceedingJoinPoint joinPoint, FeatureGate featureGate)
            throws Throwable {
        UUID tenantId = TenantContext.getTenantId();
        String featureCode = featureGate.value();

        if (!featureService.isFeatureEnabled(tenantId, featureCode)) {
            throw new FeatureNotEnabledException(
                    "Feature '" + featureCode + "' is not enabled for this restaurant. " +
                            "Please upgrade your subscription to access this feature."
            );
        }

        return joinPoint.proceed();
    }
}