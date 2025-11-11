package com.restaurant.billing.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.storage.local.menu-images}")
    private String menuImagesPath;

    @Value("${app.storage.local.logos}")
    private String logosPath;

    public String storeMenuImage(MultipartFile file) throws IOException {
        return storeFile(file, menuImagesPath);
    }

    public String storeLogo(MultipartFile file) throws IOException {
        return storeFile(file, logosPath);
    }

    private String storeFile(MultipartFile file, String directory) throws IOException {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + fileExtension;

        Path targetLocation = Paths.get(directory).resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        log.info("File stored: {}", targetLocation);
        return targetLocation.toString();
    }

    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
            log.info("File deleted: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
        }
    }
}
