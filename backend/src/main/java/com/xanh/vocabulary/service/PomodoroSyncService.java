package com.xanh.vocabulary.service;

import com.xanh.vocabulary.dto.response.PomodoroSyncResponse;
import com.xanh.vocabulary.dto.response.TopicDto;
import com.xanh.vocabulary.entity.Topic;
import com.xanh.vocabulary.entity.Vocabulary;
import com.xanh.vocabulary.enums.TopicStatus;
import com.xanh.vocabulary.repository.TopicRepository;
import com.xanh.vocabulary.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PomodoroSyncService {

    private final AiVocabSyncService aiVocabSyncService;
    private final TopicRepository topicRepository;
    private final VocabularyRepository vocabularyRepository;

    /**
     * Upsert topics and vocabulary based on AI-analyzed keywords.
     *
     * <ul>
     *   <li><b>EXISTS</b> — appends new vocabulary to the existing topic
     *   <li><b>NOT EXISTS</b> — creates a new Topic with all vocabulary
     * </ul>
     */
    @Transactional
    public PomodoroSyncResponse sync(List<String> keywords) {
        List<AiVocabSyncService.TopicSyncData> syncData = aiVocabSyncService.analyze(keywords);
        log.info("Pomodoro sync: {} keyword(s) → {} topic(s) resolved", keywords.size(), syncData.size());

        int created = 0, updated = 0, wordsAdded = 0;
        List<Topic> affectedTopics = new ArrayList<>();

        for (AiVocabSyncService.TopicSyncData data : syncData) {
            Optional<Topic> existing = topicRepository.findByName(data.topicName());

            if (existing.isPresent()) {
                // ── UPDATE path ───────────────────────────────────────────────
                Topic topic = existing.get();
                int added = appendNewWords(topic, data.words());
                wordsAdded += added;
                if (added > 0) updated++;
                affectedTopics.add(topic);
                log.debug("UPDATE topic '{}': {} new word(s) appended", topic.getName(), added);

            } else {
                // ── CREATE path ───────────────────────────────────────────────
                Topic newTopic = topicRepository.save(
                        Topic.builder()
                                .name(data.topicName())
                                .description("Auto-generated via Pomodoro sync")
                                .status(TopicStatus.ACTIVE)
                                .build()
                );
                int added = appendNewWords(newTopic, data.words());
                wordsAdded += added;
                created++;
                affectedTopics.add(newTopic);
                log.debug("CREATE topic '{}': {} word(s) saved", newTopic.getName(), added);
            }
        }

        log.info("Pomodoro sync complete — created: {}, updated: {}, words added: {}",
                created, updated, wordsAdded);

        List<TopicDto> topicDtos = affectedTopics.stream().map(this::toDto).toList();
        return new PomodoroSyncResponse(created, updated, wordsAdded, topicDtos);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Appends vocabulary words that do not already exist in the topic.
     *
     * @return number of words actually inserted
     */
    private int appendNewWords(Topic topic, List<ExternalVocabApiClient.WordEntry> words) {
        int count = 0;
        for (ExternalVocabApiClient.WordEntry entry : words) {
            if (!vocabularyRepository.existsByWordAndTopicId(entry.word(), topic.getId())) {
                vocabularyRepository.save(
                        Vocabulary.builder()
                                .topic(topic)
                                .word(entry.word())
                                .definition(entry.definition())
                                .pronunciation(entry.pronunciation())
                                .partOfSpeech(entry.partOfSpeech())
                                .exampleSentence(entry.exampleSentence())
                                .audioUrl(entry.audioUrl())
                                .build()
                );
                count++;
            }
        }
        return count;
    }

    private TopicDto toDto(Topic topic) {
        long count = vocabularyRepository.countByTopicId(topic.getId());
        return new TopicDto(
                topic.getId(), topic.getName(), topic.getDescription(),
                topic.getExternalApiRef(), (int) count, topic.getStatus(), topic.getCreatedAt()
        );
    }
}
