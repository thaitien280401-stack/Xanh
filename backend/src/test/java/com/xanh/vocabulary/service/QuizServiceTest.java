package com.xanh.vocabulary.service;

import com.xanh.vocabulary.dto.request.GenerateQuizRequest;
import com.xanh.vocabulary.entity.Topic;
import com.xanh.vocabulary.entity.User;
import com.xanh.vocabulary.entity.Vocabulary;
import com.xanh.vocabulary.enums.QuizType;
import com.xanh.vocabulary.kafka.producer.EventProducer;
import com.xanh.vocabulary.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock QuizRepository quizRepository;
    @Mock QuizQuestionRepository quizQuestionRepository;
    @Mock UserRepository userRepository;
    @Mock TopicRepository topicRepository;
    @Mock VocabularyRepository vocabularyRepository;
    @Mock UserVocabularyProgressRepository progressRepository;
    @Mock EventProducer eventProducer;
    @Mock ObjectMapper objectMapper;

    @InjectMocks
    QuizService quizService;

    @Test
    void generateQuiz_shouldThrow_whenNotEnoughVocabulary() {
        UUID userId = UUID.randomUUID();
        UUID topicId = UUID.randomUUID();

        User user = User.builder().username("test").email("test@test.com").passwordHash("x").build();
        Topic topic = Topic.builder().name("Tech").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(vocabularyRepository.findRandomByTopicId(eq(topicId), any(Pageable.class)))
                .thenReturn(List.of()); // empty pool

        assertThatThrownBy(() ->
                quizService.generateQuiz(userId, new GenerateQuizRequest(topicId, QuizType.MULTIPLE_CHOICE, 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not enough vocabulary");
    }

    @Test
    void generateQuiz_shouldCreateQuizWithCorrectNumberOfQuestions() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID topicId = UUID.randomUUID();

        User user = User.builder().username("test").email("test@test.com").passwordHash("x").build();
        Topic topic = Topic.builder().name("Tech").build();

        List<Vocabulary> vocabPool = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            vocabPool.add(Vocabulary.builder()
                    .topic(topic)
                    .word("word" + i)
                    .definition("definition" + i)
                    .exampleSentence("Example with word" + i + " in sentence.")
                    .build());
        }

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));
        when(vocabularyRepository.findRandomByTopicId(eq(topicId), any(Pageable.class))).thenReturn(vocabPool);
        when(quizRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(quizQuestionRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("[\"opt1\",\"opt2\",\"opt3\",\"opt4\"]");

        var result = quizService.generateQuiz(userId, new GenerateQuizRequest(topicId, QuizType.MULTIPLE_CHOICE, 5));

        assertThat(result).isNotNull();
        assertThat(result.totalQuestions()).isEqualTo(5);
        assertThat(result.quizType()).isEqualTo(QuizType.MULTIPLE_CHOICE);
    }
}
