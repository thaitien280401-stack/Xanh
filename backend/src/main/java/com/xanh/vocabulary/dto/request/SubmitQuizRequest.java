package com.xanh.vocabulary.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record SubmitQuizRequest(
        @NotNull Map<UUID, String> answers,
        @NotNull Integer timeTakenSeconds
) {}
