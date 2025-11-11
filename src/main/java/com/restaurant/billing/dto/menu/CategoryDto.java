package com.restaurant.billing.dto.menu;

import com.restaurant.billing.entity.Category;
import com.restaurant.billing.entity.MenuItem;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private UUID id;
    private String name;
    private Integer displayOrder;

    public static CategoryDto fromEntity(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .displayOrder(category.getDisplayOrder())
                .build();
    }
}