package com.xanh.vocabulary.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 3, max = 50) String username,
        String avatarUrl
) {}
