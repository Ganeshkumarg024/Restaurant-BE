package com.restaurant.billing.dto.menu;

import com.restaurant.billing.entity.MenuItem;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDto {
    private UUID id;
    private UUID categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private String imagePath;
    private Boolean isAvailable;
    private Boolean isVeg;
    private Integer preparationTime;

    public static MenuItemDto fromEntity(MenuItem item) {
        return MenuItemDto.builder()
                .id(item.getId())
                .categoryId(item.getCategory() != null ? item.getCategory().getId() : null)
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .imagePath(item.getImagePath())
                .isAvailable(item.getIsAvailable())
                .isVeg(item.getIsVeg())
                .preparationTime(item.getPreparationTime())
                .build();
    }
}

