package com.xanh.vocabulary.kafka.producer;

import com.xanh.vocabulary.event.PomodoroCompletedEvent;
import com.xanh.vocabulary.event.QuizCompletedEvent;
import com.xanh.vocabulary.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishQuizCompleted(QuizCompletedEvent event) {
        log.debug("Publishing quiz completed event: quizId={}, userId={}", event.quizId(), event.userId());
        kafkaTemplate.send(KafkaTopics.QUIZ_COMPLETED, event.userId().toString(), event);
    }

    public void publishPomodoroCompleted(PomodoroCompletedEvent event) {
        log.debug("Publishing pomodoro completed event: sessionId={}, userId={}", event.sessionId(), event.userId());
        kafkaTemplate.send(KafkaTopics.POMODORO_COMPLETED, event.userId().toString(), event);
    }
}
