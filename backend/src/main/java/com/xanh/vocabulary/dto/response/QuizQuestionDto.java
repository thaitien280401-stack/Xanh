package com.xanh.vocabulary.dto.response;

import java.util.List;
import java.util.UUID;

public record QuizQuestionDto(
        UUID id,
        UUID vocabularyId,
        String word,
        String questionText,
        List<String> options,
        String correctAnswer,
        String userAnswer,
        Boolean isCorrect
) {}
