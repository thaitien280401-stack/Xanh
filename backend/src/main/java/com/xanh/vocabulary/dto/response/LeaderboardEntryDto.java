package com.xanh.vocabulary.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record LeaderboardEntryDto(
        long rank,
        UUID userId,
        String username,
        String avatarUrl,
        Integer totalWordsLearned,
        Integer totalStudyMinutes,
        Integer totalQuizzes,
        BigDecimal highestQuizScore,
        Integer weeklyPoints,
        Integer totalPoints
) {}
