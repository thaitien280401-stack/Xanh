package com.xanh.vocabulary.dto.response;

import com.xanh.vocabulary.enums.SessionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PomodoroSessionDto(
        UUID id,
        UUID topicId,
        String topicName,
        SessionStatus status,
        Integer durationMinutes,
        Integer wordsStudied,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        LocalDateTime createdAt
) {}
