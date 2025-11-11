package com.restaurant.billing.dto.sync;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictDto {
    private UUID entityId;
    private String entityType;
    private String reason;
}
