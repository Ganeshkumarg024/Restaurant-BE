package com.restaurant.billing.service;

import com.restaurant.billing.dto.menu.CategoryDto;
import com.restaurant.billing.dto.menu.CreateCategoryRequest;
import com.restaurant.billing.dto.menu.UpdateCategoryRequest;
import com.restaurant.billing.entity.Category;
import com.restaurant.billing.entity.User;
import com.restaurant.billing.exception.ResourceNotFoundException;
import com.restaurant.billing.exception.UnauthorizedException;
import com.restaurant.billing.repository.CategoryRepository;
import com.restaurant.billing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories(String userEmail, Boolean activeOnly) {
        User user = getUserByEmail(userEmail);
        UUID tenantId = user.getTenant().getId();  // FIXED: Access tenant ID through relationship

        List<Category> categories;
        if (activeOnly) {
            categories = categoryRepository.findByTenantIdAndIsActiveOrderByDisplayOrder(tenantId, true);
        } else {
            categories = categoryRepository.findByTenantIdOrderByDisplayOrder(tenantId);
        }

        log.debug("Found {} categories for tenant {}", categories.size(), tenantId);
        return categories.stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(UUID id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        validateTenantAccess(category, user.getTenant().getId());  // FIXED

        return CategoryDto.fromEntity(category);
    }

    @Transactional
    public CategoryDto createCategory(CreateCategoryRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        UUID tenantId = user.getTenant().getId();  // FIXED

        // If displayOrder is not provided, set it to max + 1
        Integer displayOrder = request.getDisplayOrder();
        if (displayOrder == null) {
            displayOrder = categoryRepository.findMaxDisplayOrderByTenantId(tenantId)
                    .orElse(0) + 1;
        }

        Category category = Category.builder()
                .tenantId(tenantId)
                .name(request.getName())
                .displayOrder(displayOrder)
                .isActive(true)
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("Created category {} for tenant {}", savedCategory.getId(), tenantId);

        return CategoryDto.fromEntity(savedCategory);
    }

    @Transactional
    public CategoryDto updateCategory(UUID id, UpdateCategoryRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        validateTenantAccess(category, user.getTenant().getId());  // FIXED

        // Update fields
        category.setName(request.getName());

        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        Category updatedCategory = categoryRepository.save(category);
        log.info("Updated category {} for tenant {}", id, user.getTenant().getId());  // FIXED

        return CategoryDto.fromEntity(updatedCategory);
    }

    @Transactional
    public void deleteCategory(UUID id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        validateTenantAccess(category, user.getTenant().getId());  // FIXED

        // Soft delete
        category.setIsActive(false);
        categoryRepository.save(category);

        log.info("Deleted (soft) category {} for tenant {}", id, user.getTenant().getId());  // FIXED
    }

    @Transactional
    public CategoryDto updateDisplayOrder(UUID id, Integer displayOrder, String userEmail) {
        User user = getUserByEmail(userEmail);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        validateTenantAccess(category, user.getTenant().getId());  // FIXED

        category.setDisplayOrder(displayOrder);
        Category updatedCategory = categoryRepository.save(category);

        log.info("Updated display order for category {} to {}", id, displayOrder);

        return CategoryDto.fromEntity(updatedCategory);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private void validateTenantAccess(Category category, UUID tenantId) {
        if (!category.getTenantId().equals(tenantId)) {
            throw new UnauthorizedException("Access denied to this category");
        }
    }
}
