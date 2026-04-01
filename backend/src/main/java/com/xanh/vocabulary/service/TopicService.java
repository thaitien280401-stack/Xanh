package com.xanh.vocabulary.service;

import com.xanh.vocabulary.dto.request.CreateTopicRequest;
import com.xanh.vocabulary.dto.response.TopicDto;
import com.xanh.vocabulary.entity.Topic;
import com.xanh.vocabulary.enums.TopicStatus;
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

    public Page<TopicDto> getPaged(TopicStatus status, Pageable pageable) {
        return topicRepository.findByStatus(status, pageable).map(this::toDto);
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

    @Transactional
    public TopicDto updateStatus(UUID id, TopicStatus status) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Topic not found: " + id));
        topic.setStatus(status);
        return toDto(topicRepository.save(topic));
    }

    private TopicDto toDto(Topic topic) {
        long count = vocabularyRepository.countByTopicId(topic.getId());
        return new TopicDto(
                topic.getId(), topic.getName(), topic.getDescription(),
                topic.getExternalApiRef(), (int) count, topic.getStatus(), topic.getCreatedAt()
        );
    }
}
