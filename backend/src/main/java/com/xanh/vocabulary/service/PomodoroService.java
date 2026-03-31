package com.xanh.vocabulary.service;

import com.xanh.vocabulary.dto.request.StartSessionRequest;
import com.xanh.vocabulary.dto.response.PomodoroSessionDto;
import com.xanh.vocabulary.entity.PomodoroSession;
import com.xanh.vocabulary.entity.Topic;
import com.xanh.vocabulary.entity.User;
import com.xanh.vocabulary.enums.SessionStatus;
import com.xanh.vocabulary.event.PomodoroCompletedEvent;
import com.xanh.vocabulary.kafka.producer.EventProducer;
import com.xanh.vocabulary.repository.PomodoroSessionRepository;
import com.xanh.vocabulary.repository.TopicRepository;
import com.xanh.vocabulary.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PomodoroService {

    private final PomodoroSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final EventProducer eventProducer;

    @Transactional
    public PomodoroSessionDto startSession(UUID userId, StartSessionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Topic topic = topicRepository.findById(request.topicId())
                .orElseThrow(() -> new EntityNotFoundException("Topic not found"));

        PomodoroSession session = PomodoroSession.builder()
                .user(user)
                .topic(topic)
                .durationMinutes(request.durationMinutes())
                .startedAt(LocalDateTime.now())
                .build();

        return toDto(sessionRepository.save(session));
    }

    public Page<PomodoroSessionDto> getSessions(UUID userId, Pageable pageable) {
        return sessionRepository.findByUserIdOrderByStartedAtDesc(userId, pageable).map(this::toDto);
    }

    public PomodoroSessionDto getSession(UUID userId, UUID sessionId) {
        return toDto(sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found")));
    }

    @Transactional
    public PomodoroSessionDto completeSession(UUID userId, UUID sessionId, Integer wordsStudied) {
        PomodoroSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));

        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Session is not in progress");
        }

        session.setStatus(SessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());
        session.setWordsStudied(wordsStudied != null ? wordsStudied : 0);
        PomodoroSession saved = sessionRepository.save(session);

        eventProducer.publishPomodoroCompleted(new PomodoroCompletedEvent(
                userId, saved.getId(), saved.getWordsStudied(), saved.getDurationMinutes()
        ));

        return toDto(saved);
    }

    @Transactional
    public PomodoroSessionDto abandonSession(UUID userId, UUID sessionId) {
        PomodoroSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found"));

        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Session is not in progress");
        }

        session.setStatus(SessionStatus.ABANDONED);
        session.setEndedAt(LocalDateTime.now());
        return toDto(sessionRepository.save(session));
    }

    private PomodoroSessionDto toDto(PomodoroSession s) {
        return new PomodoroSessionDto(
                s.getId(), s.getTopic().getId(), s.getTopic().getName(),
                s.getStatus(), s.getDurationMinutes(), s.getWordsStudied(),
                s.getStartedAt(), s.getEndedAt(), s.getCreatedAt()
        );
    }
}
