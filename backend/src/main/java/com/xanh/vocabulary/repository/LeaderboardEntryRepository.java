package com.xanh.vocabulary.repository;

import com.xanh.vocabulary.entity.LeaderboardEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaderboardEntryRepository extends JpaRepository<LeaderboardEntry, UUID> {
    Optional<LeaderboardEntry> findByUserId(UUID userId);
    List<LeaderboardEntry> findAllByOrderByTotalPointsDesc(Pageable pageable);
    List<LeaderboardEntry> findAllByOrderByWeeklyPointsDesc(Pageable pageable);

    @Query("SELECT COUNT(e) + 1 FROM LeaderboardEntry e WHERE e.totalPoints > " +
           "(SELECT e2.totalPoints FROM LeaderboardEntry e2 WHERE e2.user.id = :userId)")
    long findRankByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(e) + 1 FROM LeaderboardEntry e WHERE e.weeklyPoints > " +
           "(SELECT e2.weeklyPoints FROM LeaderboardEntry e2 WHERE e2.user.id = :userId)")
    long findWeeklyRankByUserId(@Param("userId") UUID userId);
}
