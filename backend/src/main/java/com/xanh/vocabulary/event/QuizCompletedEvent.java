package com.xanh.vocabulary.event;

import java.math.BigDecimal;
import java.util.UUID;

public record QuizCompletedEvent(
        UUID userId,
        UUID quizId,
        BigDecimal score,
        UUID topicId,
        int correctAnswers
) {}
