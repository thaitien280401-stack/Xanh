package com.xanh.vocabulary.kafka.consumer;

import com.xanh.vocabulary.entity.LeaderboardEntry;
import com.xanh.vocabulary.entity.User;
import com.xanh.vocabulary.event.PomodoroCompletedEvent;
import com.xanh.vocabulary.event.QuizCompletedEvent;
import com.xanh.vocabulary.kafka.KafkaTopics;
import com.xanh.vocabulary.repository.LeaderboardEntryRepository;
import com.xanh.vocabulary.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaderboardKafkaConsumer {

    private final LeaderboardEntryRepository leaderboardEntryRepository;
    private final UserRepository userRepository;

    @KafkaListener(topics = KafkaTopics.QUIZ_COMPLETED, groupId = "leaderboard-group",
                   containerFactory = "quizCompletedListenerFactory")
    @Transactional
    public void onQuizCompleted(QuizCompletedEvent event) {
        log.info("Leaderboard consumer received quiz.completed for user {}", event.userId());
        LeaderboardEntry entry = getOrCreateEntry(event.userId());

        entry.setTotalQuizzes(entry.getTotalQuizzes() + 1);

        if (event.score().compareTo(entry.getHighestQuizScore()) > 0) {
            entry.setHighestQuizScore(event.score());
        }

        int quizPoints = event.score().intValue();
        entry.setWeeklyPoints(entry.getWeeklyPoints() + quizPoints);
        entry.setTotalPoints(entry.getTotalPoints() + quizPoints);
        entry.setLastUpdatedAt(LocalDateTime.now());

        leaderboardEntryRepository.save(entry);
    }

    @KafkaListener(topics = KafkaTopics.POMODORO_COMPLETED, groupId = "leaderboard-group",
                   containerFactory = "pomodoroCompletedListenerFactory")
    @Transactional
    public void onPomodoroCompleted(PomodoroCompletedEvent event) {
        log.info("Leaderboard consumer received pomodoro.completed for user {}", event.userId());
        LeaderboardEntry entry = getOrCreateEntry(event.userId());

        entry.setTotalStudyMinutes(entry.getTotalStudyMinutes() + event.durationMinutes());
        entry.setTotalWordsLearned(entry.getTotalWordsLearned() + event.wordsStudied());

        int studyPoints = event.durationMinutes() + event.wordsStudied() * 2;
        entry.setWeeklyPoints(entry.getWeeklyPoints() + studyPoints);
        entry.setTotalPoints(entry.getTotalPoints() + studyPoints);
        entry.setLastUpdatedAt(LocalDateTime.now());

        leaderboardEntryRepository.save(entry);
    }

    private LeaderboardEntry getOrCreateEntry(UUID userId) {
        return leaderboardEntryRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.getReferenceById(userId);
                    return LeaderboardEntry.builder().user(user).build();
                });
    }
}
