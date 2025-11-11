package com.restaurant.billing.controller;

import com.restaurant.billing.dto.menu.CategoryDto;
import com.restaurant.billing.dto.menu.CreateCategoryRequest;
import com.restaurant.billing.dto.menu.UpdateCategoryRequest;
import com.restaurant.billing.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "category-controller", description = "Category Management API")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories for tenant")
    public ResponseEntity<List<CategoryDto>> getAllCategories(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly) {
        List<CategoryDto> categories = categoryService.getAllCategories(userDetails.getUsername(), activeOnly);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryDto> getCategoryById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        CategoryDto category = categoryService.getCategoryById(id, userDetails.getUsername());
        return ResponseEntity.ok(category);
    }

    @PostMapping
    @Operation(summary = "Create new category")
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CreateCategoryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CategoryDto category = categoryService.createCategory(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update existing category")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CategoryDto category = categoryService.updateCategory(id, request, userDetails.getUsername());
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category (soft delete)")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        categoryService.deleteCategory(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reorder")
    @Operation(summary = "Update category display order")
    public ResponseEntity<CategoryDto> updateDisplayOrder(
            @PathVariable UUID id,
            @RequestParam Integer displayOrder,
            @AuthenticationPrincipal UserDetails userDetails) {
        CategoryDto category = categoryService.updateDisplayOrder(id, displayOrder, userDetails.getUsername());
        return ResponseEntity.ok(category);
    }
}
