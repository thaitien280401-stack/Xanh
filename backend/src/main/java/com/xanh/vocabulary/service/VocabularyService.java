package com.xanh.vocabulary.service;

import com.xanh.vocabulary.dto.response.VocabularyDto;
import com.xanh.vocabulary.entity.Topic;
import com.xanh.vocabulary.entity.Vocabulary;
import com.xanh.vocabulary.repository.TopicRepository;
import com.xanh.vocabulary.repository.VocabularyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final TopicRepository topicRepository;
    private final ExternalVocabApiClient externalApi;

    public Page<VocabularyDto> getByTopic(UUID topicId, Pageable pageable) {
        return vocabularyRepository.findByTopicId(topicId, pageable).map(this::toDto);
    }

    public VocabularyDto getById(UUID id) {
        return toDto(vocabularyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vocabulary not found: " + id)));
    }

    public Page<VocabularyDto> search(String query, Pageable pageable) {
        return vocabularyRepository.searchByQuery(query, pageable).map(this::toDto);
    }

    @Transactional
    public List<VocabularyDto> importFromExternalApi(UUID topicId, String word) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new EntityNotFoundException("Topic not found: " + topicId));

        List<ExternalVocabApiClient.WordEntry> entries = externalApi.fetchWord(word);
        if (entries.isEmpty()) {
            throw new EntityNotFoundException("No entries found for word: " + word);
        }

        List<Vocabulary> saved = entries.stream()
                .filter(e -> !vocabularyRepository.existsByWordAndTopicId(e.word(), topicId))
                .map(e -> Vocabulary.builder()
                        .topic(topic)
                        .word(e.word())
                        .definition(e.definition())
                        .pronunciation(e.pronunciation())
                        .partOfSpeech(e.partOfSpeech())
                        .exampleSentence(e.exampleSentence())
                        .audioUrl(e.audioUrl())
                        .build())
                .map(vocabularyRepository::save)
                .toList();

        return saved.stream().map(this::toDto).toList();
    }

    private VocabularyDto toDto(Vocabulary v) {
        return new VocabularyDto(
                v.getId(), v.getTopic().getId(), v.getTopic().getName(),
                v.getWord(), v.getDefinition(), v.getPronunciation(),
                v.getPartOfSpeech(), v.getExampleSentence(), v.getAudioUrl(),
                v.getImageUrl(), v.getDifficulty(), v.getCreatedAt()
        );
    }
}
