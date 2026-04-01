package com.xanh.vocabulary.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record SchedulerConfigDto(
        UUID id,
        String configKey,
        String configValue,
        String description,
        LocalDateTime updatedAt
) {}
