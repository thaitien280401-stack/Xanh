package com.xanh.vocabulary.repository;

import com.xanh.vocabulary.entity.PomodoroSession;
import com.xanh.vocabulary.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, UUID> {
    Page<PomodoroSession> findByUserIdOrderByStartedAtDesc(UUID userId, Pageable pageable);
    Optional<PomodoroSession> findByIdAndUserId(UUID id, UUID userId);
    List<PomodoroSession> findByUserIdAndStatus(UUID userId, SessionStatus status);

    @Query("SELECT COALESCE(SUM(p.durationMinutes), 0) FROM PomodoroSession p " +
           "WHERE p.user.id = :userId AND p.status = 'COMPLETED'")
    Integer sumCompletedDurationByUserId(@Param("userId") UUID userId);

    @Query("SELECT COALESCE(SUM(p.wordsStudied), 0) FROM PomodoroSession p " +
           "WHERE p.user.id = :userId AND p.status = 'COMPLETED'")
    Integer sumWordsStudiedByUserId(@Param("userId") UUID userId);
}
