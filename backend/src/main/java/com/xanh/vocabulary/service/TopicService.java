package com.xanh.vocabulary.service;

import com.xanh.vocabulary.dto.request.CreateTopicRequest;
import com.xanh.vocabulary.dto.response.TopicDto;
import com.xanh.vocabulary.entity.Topic;
import com.xanh.vocabulary.repository.TopicRepository;
import com.xanh.vocabulary.repository.VocabularyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final VocabularyRepository vocabularyRepository;

    public List<TopicDto> getAll() {
        return topicRepository.findAll().stream().map(this::toDto).toList();
    }

    public TopicDto getById(UUID id) {
        return toDto(topicRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Topic not found: " + id)));
    }

    @Transactional
    public TopicDto create(CreateTopicRequest request) {
        if (topicRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Topic with this name already exists");
        }
        Topic topic = Topic.builder()
                .name(request.name())
                .description(request.description())
                .externalApiRef(request.externalApiRef())
                .build();
        return toDto(topicRepository.save(topic));
    }

    private TopicDto toDto(Topic topic) {
        int count = vocabularyRepository.findByTopicId(topic.getId()).size();
        return new TopicDto(
                topic.getId(), topic.getName(), topic.getDescription(),
                topic.getExternalApiRef(), count, topic.getCreatedAt()
        );
    }
}
