package com.xanh.vocabulary.repository;

import com.xanh.vocabulary.entity.UserVocabularyProgress;
import com.xanh.vocabulary.enums.MasteryLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserVocabularyProgressRepository extends JpaRepository<UserVocabularyProgress, UUID> {
    Optional<UserVocabularyProgress> findByUserIdAndVocabularyId(UUID userId, UUID vocabularyId);
    List<UserVocabularyProgress> findByUserId(UUID userId);
    List<UserVocabularyProgress> findByUserIdAndVocabulary_TopicId(UUID userId, UUID topicId);

    @Query("SELECT COUNT(p) FROM UserVocabularyProgress p " +
           "WHERE p.user.id = :userId AND p.masteryLevel = :level")
    long countByUserIdAndMasteryLevel(@Param("userId") UUID userId, @Param("level") MasteryLevel level);
}
