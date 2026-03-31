package com.xanh.vocabulary.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record TopicDto(
        UUID id,
        String name,
        String description,
        String externalApiRef,
        int vocabularyCount,
        LocalDateTime createdAt
) {}
