package com.xanh.vocabulary.dto.request;

import com.xanh.vocabulary.enums.QuizType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GenerateQuizRequest(
        @NotNull UUID topicId,
        @NotNull QuizType quizType,
        @NotNull @Min(5) @Max(50) Integer questionCount
) {}
