package com.xanh.vocabulary.dto.response;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UUID userId,
        String username,
        String email,
        String role
) {}
