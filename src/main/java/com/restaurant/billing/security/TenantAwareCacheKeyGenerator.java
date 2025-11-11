package com.restaurant.billing.config;

import com.restaurant.billing.security.TenantContext;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;

@Component("tenantAwareKeyGenerator")
public class TenantAwareCacheKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        UUID tenantId = TenantContext.getTenantId();
        return tenantId != null ? tenantId.toString() : "default";
    }
}
