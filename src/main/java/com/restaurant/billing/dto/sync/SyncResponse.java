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
public class SyncResponse {
    private Boolean success;
    private List<SyncResultDto> results;
    private List<ConflictDto> conflicts;
    private LocalDateTime timestamp;
}
