package com.xanh.vocabulary.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartSessionRequest(
        @NotNull UUID topicId,
        @NotNull @Min(5) @Max(120) Integer durationMinutes
) {}
