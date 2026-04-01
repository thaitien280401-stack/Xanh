package com.xanh.vocabulary.service;

import com.xanh.vocabulary.dto.response.PomodoroSyncResponse;
import com.xanh.vocabulary.entity.Topic;
import com.xanh.vocabulary.entity.Vocabulary;
import com.xanh.vocabulary.enums.TopicStatus;
import com.xanh.vocabulary.repository.TopicRepository;
import com.xanh.vocabulary.repository.VocabularyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PomodoroSyncServiceTest {

    @Mock AiVocabSyncService aiVocabSyncService;
    @Mock TopicRepository topicRepository;
    @Mock VocabularyRepository vocabularyRepository;

    @InjectMocks PomodoroSyncService syncService;

    private static final UUID TOPIC_A_ID = UUID.randomUUID();

    private Topic topicA;
    private AiVocabSyncService.TopicSyncData syncDataA;
    private AiVocabSyncService.TopicSyncData syncDataB;

    @BeforeEach
    void setUp() {
        topicA = new Topic();
        topicA.setName("Topic A");
        topicA.setStatus(TopicStatus.ACTIVE);
        // Simulate a managed entity with an ID
        topicA = spy(topicA);
        doReturn(TOPIC_A_ID).when(topicA).getId();

        ExternalVocabApiClient.WordEntry wordAlgorithm =
                new ExternalVocabApiClient.WordEntry("algorithm", "A step-by-step procedure", "/ˈælɡərɪðəm/", "noun", "An algorithm sorts data.", null);
        ExternalVocabApiClient.WordEntry wordCompiler =
                new ExternalVocabApiClient.WordEntry("compiler", "Translates source code", "/kəmˈpaɪlər/", "noun", "The compiler found an error.", null);

        syncDataA = new AiVocabSyncService.TopicSyncData("Topic A", List.of(wordAlgorithm, wordCompiler));
        syncDataB = new AiVocabSyncService.TopicSyncData("Topic B", List.of(wordAlgorithm));
    }

    // ── UPDATE path ────────────────────────────────────────────────────────

    @Test
    void sync_updatesTopic_whenTopicAExists() {
        when(aiVocabSyncService.analyze(any())).thenReturn(List.of(syncDataA));
        when(topicRepository.findByName("Topic A")).thenReturn(Optional.of(topicA));
        when(vocabularyRepository.existsByWordAndTopicId(any(), eq(TOPIC_A_ID))).thenReturn(false);
        when(vocabularyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(vocabularyRepository.countByTopicId(TOPIC_A_ID)).thenReturn(2L);

        PomodoroSyncResponse result = syncService.sync(List.of("topic a"));

        assertThat(result.updated()).isEqualTo(1);
        assertThat(result.created()).isEqualTo(0);
        assertThat(result.wordsAdded()).isEqualTo(2);
        // Topic itself must NOT be created again
        verify(topicRepository, never()).save(any(Topic.class));
    }

    @Test
    void sync_skipsExistingWords_whenTopicAExists() {
        when(aiVocabSyncService.analyze(any())).thenReturn(List.of(syncDataA));
        when(topicRepository.findByName("Topic A")).thenReturn(Optional.of(topicA));
        // "algorithm" already exists, "compiler" is new
        when(vocabularyRepository.existsByWordAndTopicId("algorithm", TOPIC_A_ID)).thenReturn(true);
        when(vocabularyRepository.existsByWordAndTopicId("compiler",  TOPIC_A_ID)).thenReturn(false);
        when(vocabularyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(vocabularyRepository.countByTopicId(TOPIC_A_ID)).thenReturn(5L);

        PomodoroSyncResponse result = syncService.sync(List.of("topic a"));

        assertThat(result.wordsAdded()).isEqualTo(1);    // only "compiler" added
        assertThat(result.updated()).isEqualTo(1);

        ArgumentCaptor<Vocabulary> vocabCaptor = ArgumentCaptor.forClass(Vocabulary.class);
        verify(vocabularyRepository, times(1)).save(vocabCaptor.capture());
        assertThat(vocabCaptor.getValue().getWord()).isEqualTo("compiler");
    }

    @Test
    void sync_doesNotCountUpdate_whenNoNewWordsForExistingTopic() {
        when(aiVocabSyncService.analyze(any())).thenReturn(List.of(syncDataA));
        when(topicRepository.findByName("Topic A")).thenReturn(Optional.of(topicA));
        // All words already exist
        when(vocabularyRepository.existsByWordAndTopicId(any(), eq(TOPIC_A_ID))).thenReturn(true);
        when(vocabularyRepository.countByTopicId(TOPIC_A_ID)).thenReturn(5L);

        PomodoroSyncResponse result = syncService.sync(List.of("topic a"));

        assertThat(result.wordsAdded()).isEqualTo(0);
        assertThat(result.updated()).isEqualTo(0);   // nothing actually changed
        verify(vocabularyRepository, never()).save(any());
    }

    // ── CREATE path ────────────────────────────────────────────────────────

    @Test
    void sync_createsTopic_whenTopicBDoesNotExist() {
        Topic savedB = new Topic();
        savedB.setName("Topic B");
        savedB.setStatus(TopicStatus.ACTIVE);
        UUID idB = UUID.randomUUID();
        savedB = spy(savedB);
        doReturn(idB).when(savedB).getId();

        when(aiVocabSyncService.analyze(any())).thenReturn(List.of(syncDataB));
        when(topicRepository.findByName("Topic B")).thenReturn(Optional.empty());
        when(topicRepository.save(any(Topic.class))).thenReturn(savedB);
        when(vocabularyRepository.existsByWordAndTopicId(any(), eq(idB))).thenReturn(false);
        when(vocabularyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(vocabularyRepository.countByTopicId(idB)).thenReturn(1L);

        PomodoroSyncResponse result = syncService.sync(List.of("topic b"));

        assertThat(result.created()).isEqualTo(1);
        assertThat(result.updated()).isEqualTo(0);
        assertThat(result.wordsAdded()).isEqualTo(1);

        ArgumentCaptor<Topic> topicCaptor = ArgumentCaptor.forClass(Topic.class);
        verify(topicRepository).save(topicCaptor.capture());
        assertThat(topicCaptor.getValue().getName()).isEqualTo("Topic B");
        assertThat(topicCaptor.getValue().getStatus()).isEqualTo(TopicStatus.ACTIVE);
    }

    // ── Mixed: A exists, B is new ──────────────────────────────────────────

    @Test
    void sync_handlesUpdateAndCreateInSameRequest() {
        Topic savedB = new Topic();
        savedB.setName("Topic B");
        UUID idB = UUID.randomUUID();
        savedB = spy(savedB);
        doReturn(idB).when(savedB).getId();

        when(aiVocabSyncService.analyze(any())).thenReturn(List.of(syncDataA, syncDataB));
        when(topicRepository.findByName("Topic A")).thenReturn(Optional.of(topicA));
        when(topicRepository.findByName("Topic B")).thenReturn(Optional.empty());
        when(topicRepository.save(any(Topic.class))).thenReturn(savedB);
        when(vocabularyRepository.existsByWordAndTopicId(any(), eq(TOPIC_A_ID))).thenReturn(false);
        when(vocabularyRepository.existsByWordAndTopicId(any(), eq(idB))).thenReturn(false);
        when(vocabularyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(vocabularyRepository.countByTopicId(any())).thenReturn(2L);

        PomodoroSyncResponse result = syncService.sync(List.of("topic a", "topic b"));

        assertThat(result.created()).isEqualTo(1);   // Topic B
        assertThat(result.updated()).isEqualTo(1);   // Topic A
        assertThat(result.wordsAdded()).isEqualTo(3);// 2 for A + 1 for B
        assertThat(result.topics()).hasSize(2);
    }

    // ── Edge cases ────────────────────────────────────────────────────────

    @Test
    void sync_returnsEmpty_whenAiReturnsNoData() {
        when(aiVocabSyncService.analyze(any())).thenReturn(List.of());

        PomodoroSyncResponse result = syncService.sync(List.of("unknown-keyword"));

        assertThat(result.created()).isEqualTo(0);
        assertThat(result.updated()).isEqualTo(0);
        assertThat(result.wordsAdded()).isEqualTo(0);
        assertThat(result.topics()).isEmpty();
        verifyNoInteractions(topicRepository, vocabularyRepository);
    }
}
