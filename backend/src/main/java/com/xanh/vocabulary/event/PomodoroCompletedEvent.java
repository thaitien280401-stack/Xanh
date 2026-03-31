package com.xanh.vocabulary.event;

import java.util.UUID;

public record PomodoroCompletedEvent(
        UUID userId,
        UUID sessionId,
        int wordsStudied,
        int durationMinutes
) {}
