package com.xanh.vocabulary.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDto(
        UUID id,
        String username,
        String email,
        String avatarUrl,
        String role,
        LocalDateTime createdAt
) {}
