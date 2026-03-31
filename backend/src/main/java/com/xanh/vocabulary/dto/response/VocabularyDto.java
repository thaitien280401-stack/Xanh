package com.xanh.vocabulary.dto.response;

import com.xanh.vocabulary.enums.Difficulty;

import java.time.LocalDateTime;
import java.util.UUID;

public record VocabularyDto(
        UUID id,
        UUID topicId,
        String topicName,
        String word,
        String definition,
        String pronunciation,
        String partOfSpeech,
        String exampleSentence,
        String audioUrl,
        String imageUrl,
        Difficulty difficulty,
        LocalDateTime createdAt
) {}
