package com.restaurant.billing.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.File;

@Slf4j
@Configuration
public class StorageConfig {

    @Value("${app.storage.local.base-path}")
    private String basePath;

    @Value("${app.storage.local.menu-images}")
    private String menuImagesPath;

    @Value("${app.storage.local.invoices}")
    private String invoicesPath;

    @Value("${app.storage.local.qr-codes}")
    private String qrCodesPath;

    @Value("${app.storage.local.logos}")
    private String logosPath;

    @PostConstruct
    public void init() {
        createDirectory(basePath);
        createDirectory(menuImagesPath);
        createDirectory(invoicesPath);
        createDirectory(qrCodesPath);
        createDirectory(logosPath);
    }

    private void createDirectory(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("Created directory: {}", path);
            } else {
                log.warn("Failed to create directory: {}", path);
            }
        }
    }
}
