package com.xanh.vocabulary.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PomodoroSyncRequest(
        @NotEmpty(message = "At least one keyword is required")
        @Size(max = 20, message = "Max 20 keywords per sync")
        List<@jakarta.validation.constraints.NotBlank String> keywords
) {}
