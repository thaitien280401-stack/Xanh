package com.xanh.vocabulary.service;

import com.xanh.vocabulary.dto.request.AiSyncRequest;
import com.xanh.vocabulary.dto.response.AiSyncResponse;
import com.xanh.vocabulary.entity.Topic;
import com.xanh.vocabulary.entity.Vocabulary;
import com.xanh.vocabulary.enums.Difficulty;
import com.xanh.vocabulary.enums.TopicStatus;
import com.xanh.vocabulary.repository.TopicRepository;
import com.xanh.vocabulary.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * High-volume AI vocabulary sync — batch-optimised upsert.
 *
 * <h3>Performance profile (400 words)</h3>
 * <pre>
 *   Old (PomodoroSyncService)    New (this service)
 *   ────────────────────────     ─────────────────────────────
 *   400 × SELECT (existsBy…)     1 × SELECT (findWordsByTopicId)
 *   400 × INSERT (save)          8 × batch INSERT (saveAll, size 50)
 *   ≈ 800 round-trips            ≈ 9 round-trips
 * </pre>
 *
 * <p>Batch inserts work because {@code BaseEntity} uses
 * {@code GenerationType.UUID} — PKs are pre-assigned in Java so
 * Hibernate never needs a select-after-insert.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiHighVolumeSyncService {

    private final HighVolumeWordGenerator wordGenerator;
    private final TopicRepository         topicRepository;
    private final VocabularyRepository    vocabularyRepository;

    /**
     * Generates {@code request.wordCount()} vocabulary entries for
     * {@code request.topicName()}, then batch-upserts them.
     *
     * <ol>
     *   <li>Generate word list (placeholder AI)
     *   <li>Find or create the topic
     *   <li>Fetch all existing words in ONE query → {@code Set<String>}
     *   <li>Deduplicate with {@code stream().filter()} (O(1) per lookup)
     *   <li>Batch-insert new words with {@code saveAll()}
     * </ol>
     *
     * @param request validated request DTO
     * @return sync result with counts and timing
     */
    @Transactional
    public AiSyncResponse sync(AiSyncRequest request) {
        long start = System.currentTimeMillis();

        // ── Step 1: generate words ────────────────────────────────────────────
        List<HighVolumeWordGenerator.WordEntry> generated =
                wordGenerator.generate(request.topicName(), request.wordCount());
        int totalGenerated = generated.size();
        log.info("AI sync: generated {} word(s) for topic '{}'", totalGenerated, request.topicName());

        // ── Step 2: upsert topic ──────────────────────────────────────────────
        Topic topic = upsertTopic(request.topicName());

        // ── Step 3: ONE query for existing words ──────────────────────────────
        Set<String> existingWords = vocabularyRepository.findWordsByTopicId(topic.getId());
        log.debug("AI sync: {} existing word(s) in topic '{}'", existingWords.size(), topic.getName());

        // ── Step 4: deduplicate via stream().filter() ─────────────────────────
        List<Vocabulary> newVocabs = generated.stream()
                .filter(w -> !existingWords.contains(w.word().toLowerCase()))
                .map(w -> toEntity(w, topic))
                .toList();

        int skipped = totalGenerated - newVocabs.size();
        log.debug("AI sync: {} new / {} duplicate(s) filtered out", newVocabs.size(), skipped);

        // ── Step 5: batch insert ──────────────────────────────────────────────
        if (!newVocabs.isEmpty()) {
            vocabularyRepository.saveAll(newVocabs);   // Hibernate batches in groups of 50
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("AI sync complete — topic='{}', generated={}, saved={}, skipped={}, {}ms",
                topic.getName(), totalGenerated, newVocabs.size(), skipped, elapsed);

        return new AiSyncResponse(
                topic.getId(),
                topic.getName(),
                totalGenerated,
                newVocabs.size(),
                skipped,
                elapsed
        );
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Topic upsertTopic(String topicName) {
        Optional<Topic> existing = topicRepository.findByName(topicName);
        if (existing.isPresent()) {
            log.debug("AI sync: reusing existing topic '{}'", topicName);
            return existing.get();
        }
        Topic created = topicRepository.save(
                Topic.builder()
                        .name(topicName)
                        .description("Auto-generated via AI high-volume sync")
                        .status(TopicStatus.ACTIVE)
                        .build()
        );
        log.debug("AI sync: created new topic '{}' (id={})", topicName, created.getId());
        return created;
    }

    private static Vocabulary toEntity(HighVolumeWordGenerator.WordEntry w, Topic topic) {
        Difficulty difficulty;
        try {
            difficulty = Difficulty.valueOf(w.difficulty());
        } catch (IllegalArgumentException | NullPointerException e) {
            difficulty = Difficulty.MEDIUM;
        }
        return Vocabulary.builder()
                .topic(topic)
                .word(w.word())
                .definition(w.definition())
                .pronunciation(w.pronunciation())
                .partOfSpeech(w.partOfSpeech())
                .exampleSentence(w.exampleSentence())
                .audioUrl(w.audioUrl())
                .difficulty(difficulty)
                .build();
    }
}
