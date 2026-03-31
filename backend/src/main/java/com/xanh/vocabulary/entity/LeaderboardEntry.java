package com.xanh.vocabulary.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "leaderboard_entries", indexes = {
        @Index(name = "idx_leaderboard_total_points", columnList = "total_points DESC"),
        @Index(name = "idx_leaderboard_weekly_points", columnList = "weekly_points DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntry extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "total_words_learned", nullable = false)
    @Builder.Default
    private Integer totalWordsLearned = 0;

    @Column(name = "total_study_minutes", nullable = false)
    @Builder.Default
    private Integer totalStudyMinutes = 0;

    @Column(name = "total_quizzes", nullable = false)
    @Builder.Default
    private Integer totalQuizzes = 0;

    @Column(name = "highest_quiz_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal highestQuizScore = BigDecimal.ZERO;

    @Column(name = "weekly_points", nullable = false)
    @Builder.Default
    private Integer weeklyPoints = 0;

    @Column(name = "total_points", nullable = false)
    @Builder.Default
    private Integer totalPoints = 0;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;
}
