package com.restaurant.billing.service;

import com.restaurant.billing.dto.menu.*;
import com.restaurant.billing.entity.*;
import com.restaurant.billing.exception.ResourceNotFoundException;
import com.restaurant.billing.repository.*;
import com.restaurant.billing.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;

//    @Cacheable(value = "menu-items", key = "#tenantId")
    @Transactional(readOnly = true)
    public List<MenuItemDto> getAllMenuItems(UUID tenantId) {
        return menuItemRepository.findByTenantIdAndIsDeleted(tenantId, false)
                .stream()
                .map(MenuItemDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MenuItemDto getMenuItemById(UUID id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        return MenuItemDto.fromEntity(item); // call the static method directly
    }


    //@CacheEvict(value = "menu-items", key = "#tenantContext.tenantId")
    @Transactional
    public MenuItemDto createMenuItem(MenuItemDto dto, String deviceId) {
        UUID tenantId = TenantContext.getTenantId();

        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        MenuItem item = MenuItem.builder()
                .tenantId(tenantId)
                .category(category)
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .imagePath(dto.getImagePath())
                .isAvailable(dto.getIsAvailable() != null ? dto.getIsAvailable() : true)
                .isVeg(dto.getIsVeg() != null ? dto.getIsVeg() : true)
                .preparationTime(dto.getPreparationTime())
                .isDeleted(false)
                .deviceId(deviceId)
                .version(1L)
                .build();

        MenuItem saved = menuItemRepository.save(item);
        return MenuItemDto.fromEntity(saved);
    }

    //@CacheEvict(value = "menu-items", key = "#tenantContext.tenantId")
    @Transactional
    public MenuItemDto updateMenuItem(UUID id, MenuItemDto dto, String deviceId) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            item.setCategory(category);
        }

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setIsAvailable(dto.getIsAvailable());
        item.setIsVeg(dto.getIsVeg());
        item.setPreparationTime(dto.getPreparationTime());
        item.setDeviceId(deviceId);
        item.setVersion(item.getVersion() + 1);
        item.setSyncedAt(null); // Mark for sync

        MenuItem updated = menuItemRepository.save(item);
        return MenuItemDto.fromEntity(updated);
    }

    //@CacheEvict(value = "menu-items", key = "#tenantContext.tenantId")
    @Transactional
    public void deleteMenuItem(UUID id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        item.setIsDeleted(true);
        item.setSyncedAt(null);
        menuItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories(UUID tenantId) {
        return categoryRepository.findByTenantIdAndIsActiveOrderByDisplayOrder(tenantId, true)
                .stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
    }
}