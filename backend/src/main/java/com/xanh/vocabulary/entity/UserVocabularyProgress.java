package com.xanh.vocabulary.entity;

import com.xanh.vocabulary.enums.MasteryLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_vocabulary_progress", indexes = {
        @Index(name = "idx_progress_user_id", columnList = "user_id"),
        @Index(name = "idx_progress_vocabulary_id", columnList = "vocabulary_id"),
        @Index(name = "idx_progress_next_review", columnList = "next_review_at")
},
uniqueConstraints = {
        @UniqueConstraint(name = "uq_progress_user_vocab", columnNames = {"user_id", "vocabulary_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVocabularyProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id", nullable = false)
    private Vocabulary vocabulary;

    @Column(name = "times_seen", nullable = false)
    @Builder.Default
    private Integer timesSeen = 0;

    @Column(name = "times_correct", nullable = false)
    @Builder.Default
    private Integer timesCorrect = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "mastery_level", nullable = false, length = 20)
    @Builder.Default
    private MasteryLevel masteryLevel = MasteryLevel.NEW;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt;
}
