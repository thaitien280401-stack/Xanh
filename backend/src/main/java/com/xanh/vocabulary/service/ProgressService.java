package com.xanh.vocabulary.service;

import com.xanh.vocabulary.dto.response.ProgressStatsDto;
import com.xanh.vocabulary.enums.MasteryLevel;
import com.xanh.vocabulary.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final PomodoroSessionRepository pomodoroSessionRepository;
    private final QuizRepository quizRepository;
    private final UserVocabularyProgressRepository progressRepository;

    public ProgressStatsDto getStats(UUID userId) {
        long totalSessions = pomodoroSessionRepository
                .findByUserIdOrderByStartedAtDesc(userId, org.springframework.data.domain.Pageable.unpaged())
                .getTotalElements();

        int totalStudyMinutes = pomodoroSessionRepository.sumCompletedDurationByUserId(userId);
        int totalWordsStudied = pomodoroSessionRepository.sumWordsStudiedByUserId(userId);
        long totalQuizzes = quizRepository.countCompletedByUserId(userId);
        BigDecimal avgScore = quizRepository.findAverageScoreByUserId(userId);

        Map<String, Long> masteryBreakdown = Arrays.stream(MasteryLevel.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        level -> progressRepository.countByUserIdAndMasteryLevel(userId, level)
                ));

        return new ProgressStatsDto(
                totalSessions, totalStudyMinutes, totalWordsStudied,
                totalQuizzes, avgScore, masteryBreakdown
        );
    }
}
