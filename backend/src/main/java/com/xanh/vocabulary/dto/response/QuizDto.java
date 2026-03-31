package com.xanh.vocabulary.dto.response;

import com.xanh.vocabulary.enums.QuizType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record QuizDto(
        UUID id,
        UUID topicId,
        String topicName,
        QuizType quizType,
        Integer totalQuestions,
        Integer correctAnswers,
        BigDecimal score,
        Integer timeTakenSeconds,
        LocalDateTime completedAt,
        List<QuizQuestionDto> questions,
        LocalDateTime createdAt
) {}
