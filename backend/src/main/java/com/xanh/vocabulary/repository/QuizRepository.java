package com.xanh.vocabulary.repository;

import com.xanh.vocabulary.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    Page<Quiz> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Optional<Quiz> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COALESCE(MAX(q.score), 0) FROM Quiz q WHERE q.user.id = :userId AND q.completedAt IS NOT NULL")
    BigDecimal findHighestScoreByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.user.id = :userId AND q.completedAt IS NOT NULL")
    long countCompletedByUserId(@Param("userId") UUID userId);

    @Query("SELECT COALESCE(AVG(q.score), 0) FROM Quiz q WHERE q.user.id = :userId AND q.completedAt IS NOT NULL")
    BigDecimal findAverageScoreByUserId(@Param("userId") UUID userId);
}
