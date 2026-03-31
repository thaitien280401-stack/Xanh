package com.xanh.vocabulary.service;

import com.xanh.vocabulary.dto.response.LeaderboardEntryDto;
import com.xanh.vocabulary.entity.LeaderboardEntry;
import com.xanh.vocabulary.repository.LeaderboardEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final LeaderboardEntryRepository leaderboardEntryRepository;

    public List<LeaderboardEntryDto> getTopLeaderboard(String type, int limit) {
        List<LeaderboardEntry> entries = "weekly".equalsIgnoreCase(type)
                ? leaderboardEntryRepository.findAllByOrderByWeeklyPointsDesc(PageRequest.of(0, limit))
                : leaderboardEntryRepository.findAllByOrderByTotalPointsDesc(PageRequest.of(0, limit));

        return rankEntries(entries, type);
    }

    public LeaderboardEntryDto getUserRank(UUID userId) {
        LeaderboardEntry entry = leaderboardEntryRepository.findByUserId(userId)
                .orElse(null);
        if (entry == null) return null;

        long rank = leaderboardEntryRepository.findRankByUserId(userId);
        return toDto(entry, rank);
    }

    private List<LeaderboardEntryDto> rankEntries(List<LeaderboardEntry> entries, String type) {
        List<LeaderboardEntryDto> result = new java.util.ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            result.add(toDto(entries.get(i), i + 1L));
        }
        return result;
    }

    private LeaderboardEntryDto toDto(LeaderboardEntry e, long rank) {
        return new LeaderboardEntryDto(
                rank,
                e.getUser().getId(),
                e.getUser().getUsername(),
                e.getUser().getAvatarUrl(),
                e.getTotalWordsLearned(),
                e.getTotalStudyMinutes(),
                e.getTotalQuizzes(),
                e.getHighestQuizScore(),
                e.getWeeklyPoints(),
                e.getTotalPoints()
        );
    }
}
