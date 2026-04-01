package com.xanh.vocabulary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSchedulerConfigRequest(
        @NotBlank @Size(max = 255) String value
) {}
