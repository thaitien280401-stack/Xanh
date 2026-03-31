package com.xanh.vocabulary.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quiz_questions", indexes = {
        @Index(name = "idx_quiz_question_quiz_id", columnList = "quiz_id"),
        @Index(name = "idx_quiz_question_vocab_id", columnList = "vocabulary_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id", nullable = false)
    private Vocabulary vocabulary;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "correct_answer", nullable = false, columnDefinition = "TEXT")
    private String correctAnswer;

    @Column(name = "options", columnDefinition = "TEXT")
    private String options;

    @Column(name = "user_answer", columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;
}
