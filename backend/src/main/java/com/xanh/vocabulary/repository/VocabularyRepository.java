package com.xanh.vocabulary.repository;

import com.xanh.vocabulary.entity.Vocabulary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, UUID> {
    List<Vocabulary> findByTopicId(UUID topicId);
    Page<Vocabulary> findByTopicId(UUID topicId, Pageable pageable);
    boolean existsByWordAndTopicId(String word, UUID topicId);

    @Query("SELECT v FROM Vocabulary v WHERE " +
           "LOWER(v.word) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.definition) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Vocabulary> searchByQuery(@Param("query") String query, Pageable pageable);

    @Query("SELECT v FROM Vocabulary v WHERE v.topic.id = :topicId ORDER BY FUNCTION('RANDOM')")
    List<Vocabulary> findRandomByTopicId(@Param("topicId") UUID topicId, Pageable pageable);
}
