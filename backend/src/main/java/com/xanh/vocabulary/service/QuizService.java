package com.xanh.vocabulary.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xanh.vocabulary.dto.request.GenerateQuizRequest;
import com.xanh.vocabulary.dto.request.SubmitQuizRequest;
import com.xanh.vocabulary.dto.response.QuizDto;
import com.xanh.vocabulary.dto.response.QuizQuestionDto;
import com.xanh.vocabulary.entity.*;
import com.xanh.vocabulary.enums.MasteryLevel;
import com.xanh.vocabulary.enums.QuizType;
import com.xanh.vocabulary.event.QuizCompletedEvent;
import com.xanh.vocabulary.kafka.producer.EventProducer;
import com.xanh.vocabulary.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final VocabularyRepository vocabularyRepository;
    private final UserVocabularyProgressRepository progressRepository;
    private final EventProducer eventProducer;
    private final ObjectMapper objectMapper;

    @Transactional
    public QuizDto generateQuiz(UUID userId, GenerateQuizRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Topic topic = topicRepository.findById(request.topicId())
                .orElseThrow(() -> new EntityNotFoundException("Topic not found"));

        List<Vocabulary> vocabPool = vocabularyRepository.findRandomByTopicId(
                request.topicId(), PageRequest.of(0, request.questionCount() * 4));

        if (vocabPool.size() < request.questionCount()) {
            throw new IllegalArgumentException("Not enough vocabulary in this topic. Found: " + vocabPool.size());
        }

        List<Vocabulary> selected = vocabPool.subList(0, request.questionCount());
        Quiz quiz = Quiz.builder()
                .user(user)
                .topic(topic)
                .quizType(request.quizType())
                .totalQuestions(request.questionCount())
                .build();
        quizRepository.save(quiz);

        List<QuizQuestion> questions = selected.stream()
                .map(vocab -> buildQuestion(quiz, vocab, request.quizType(), vocabPool))
                .toList();
        quizQuestionRepository.saveAll(questions);
        quiz.setQuestions(questions);

        return toDto(quiz);
    }

    @Transactional
    public QuizDto submitQuiz(UUID userId, UUID quizId, SubmitQuizRequest request) {
        Quiz quiz = quizRepository.findByIdAndUserId(quizId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found"));

        if (quiz.getCompletedAt() != null) {
            throw new IllegalArgumentException("Quiz already submitted");
        }

        int correct = 0;
        for (QuizQuestion question : quiz.getQuestions()) {
            String userAnswer = request.answers().get(question.getId());
            boolean isCorrect = question.getCorrectAnswer().equalsIgnoreCase(
                    userAnswer != null ? userAnswer.trim() : "");
            question.setUserAnswer(userAnswer);
            question.setIsCorrect(isCorrect);
            if (isCorrect) correct++;

            updateProgress(userId, question.getVocabulary(), isCorrect);
        }

        quiz.setCorrectAnswers(correct);
        quiz.setTimeTakenSeconds(request.timeTakenSeconds());
        quiz.setScore(BigDecimal.valueOf(correct)
                .divide(BigDecimal.valueOf(quiz.getTotalQuestions()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP));
        quiz.setCompletedAt(LocalDateTime.now());
        quizRepository.save(quiz);

        eventProducer.publishQuizCompleted(new QuizCompletedEvent(
                userId, quizId, quiz.getScore(), quiz.getTopic().getId(), correct
        ));

        return toDto(quiz);
    }

    public Page<QuizDto> getUserQuizzes(UUID userId, Pageable pageable) {
        return quizRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::toDto);
    }

    public QuizDto getQuiz(UUID userId, UUID quizId) {
        return toDto(quizRepository.findByIdAndUserId(quizId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found")));
    }

    private QuizQuestion buildQuestion(Quiz quiz, Vocabulary vocab, QuizType type, List<Vocabulary> pool) {
        String questionText;
        String correctAnswer;
        String optionsJson = null;

        switch (type) {
            case MULTIPLE_CHOICE -> {
                questionText = "What is the definition of: \"" + vocab.getWord() + "\"?";
                correctAnswer = vocab.getDefinition();
                List<String> options = new ArrayList<>();
                options.add(vocab.getDefinition());
                pool.stream()
                        .filter(v -> !v.getId().equals(vocab.getId()))
                        .limit(3)
                        .forEach(v -> options.add(v.getDefinition()));
                Collections.shuffle(options);
                try { optionsJson = objectMapper.writeValueAsString(options); } catch (JsonProcessingException ignored) {}
            }
            case FILL_BLANK -> {
                questionText = "Fill in the blank: " + vocab.getExampleSentence()
                        .replace(vocab.getWord(), "_____");
                correctAnswer = vocab.getWord();
            }
            default -> { // TRUE_FALSE
                boolean truth = new Random().nextBoolean();
                String pairedDef = truth ? vocab.getDefinition() :
                        pool.stream().filter(v -> !v.getId().equals(vocab.getId()))
                                .findFirst().map(Vocabulary::getDefinition).orElse(vocab.getDefinition());
                questionText = "True or False: \"" + vocab.getWord() + "\" means \"" + pairedDef + "\"";
                correctAnswer = truth ? "true" : "false";
            }
        }

        return QuizQuestion.builder()
                .quiz(quiz)
                .vocabulary(vocab)
                .questionText(questionText)
                .correctAnswer(correctAnswer)
                .options(optionsJson)
                .build();
    }

    private void updateProgress(UUID userId, Vocabulary vocabulary, boolean isCorrect) {
        User userRef = userRepository.getReferenceById(userId);
        UserVocabularyProgress progress = progressRepository
                .findByUserIdAndVocabularyId(userId, vocabulary.getId())
                .orElseGet(() -> UserVocabularyProgress.builder()
                        .user(userRef)
                        .vocabulary(vocabulary)
                        .build());

        progress.setTimesSeen(progress.getTimesSeen() + 1);
        if (isCorrect) progress.setTimesCorrect(progress.getTimesCorrect() + 1);
        progress.setLastReviewedAt(LocalDateTime.now());
        progress.setMasteryLevel(computeMastery(progress));
        progressRepository.save(progress);
    }

    private MasteryLevel computeMastery(UserVocabularyProgress p) {
        if (p.getTimesSeen() == 0) return MasteryLevel.NEW;
        double ratio = (double) p.getTimesCorrect() / p.getTimesSeen();
        if (ratio >= 0.9 && p.getTimesSeen() >= 5) return MasteryLevel.MASTERED;
        if (ratio >= 0.7) return MasteryLevel.FAMILIAR;
        if (p.getTimesSeen() >= 1) return MasteryLevel.LEARNING;
        return MasteryLevel.NEW;
    }

    @SuppressWarnings("unchecked")
    private QuizDto toDto(Quiz quiz) {
        List<QuizQuestionDto> questionDtos = quiz.getQuestions().stream().map(q -> {
            List<String> opts = List.of();
            if (q.getOptions() != null) {
                try { opts = objectMapper.readValue(q.getOptions(), List.class); } catch (Exception ignored) {}
            }
            return new QuizQuestionDto(
                    q.getId(), q.getVocabulary().getId(), q.getVocabulary().getWord(),
                    q.getQuestionText(), opts, q.getCorrectAnswer(),
                    q.getUserAnswer(), q.getIsCorrect()
            );
        }).toList();

        return new QuizDto(
                quiz.getId(), quiz.getTopic().getId(), quiz.getTopic().getName(),
                quiz.getQuizType(), quiz.getTotalQuestions(), quiz.getCorrectAnswers(),
                quiz.getScore(), quiz.getTimeTakenSeconds(), quiz.getCompletedAt(),
                questionDtos, quiz.getCreatedAt()
        );
    }
}
