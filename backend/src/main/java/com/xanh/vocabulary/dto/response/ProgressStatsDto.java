package com.xanh.vocabulary.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public record ProgressStatsDto(
        long totalSessions,
        int totalStudyMinutes,
        int totalWordsStudied,
        long totalQuizzesCompleted,
        BigDecimal averageQuizScore,
        Map<String, Long> masteryBreakdown
) {}
