package com.xanh.vocabulary.service;

import com.xanh.vocabulary.dto.request.AiSyncRequest;
import com.xanh.vocabulary.dto.response.AiSyncResponse;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiHighVolumeSyncServiceTest {

    @Mock HighVolumeWordGenerator wordGenerator;
    @Mock TopicRepository         topicRepository;
    @Mock VocabularyRepository    vocabularyRepository;

    @InjectMocks AiHighVolumeSyncService service;

    private static final UUID   TOPIC_ID   = UUID.randomUUID();
    private static final String TOPIC_NAME = "Technology";

    private Topic existingTopic;

    @BeforeEach
    void setUp() {
        existingTopic = spy(new Topic());
        existingTopic.setName(TOPIC_NAME);
        existingTopic.setStatus(TopicStatus.ACTIVE);
        doReturn(TOPIC_ID).when(existingTopic).getId();
    }

    // ── UPDATE path ────────────────────────────────────────────────────────────

    @Test
    void sync_reusesExistingTopic_andBatchInsertsNewWords() {
        // Generator returns 3 words
        List<HighVolumeWordGenerator.WordEntry> generated = List.of(
                wordEntry("algorithm"),
                wordEntry("recursion"),
                wordEntry("compiler")
        );
        when(wordGenerator.generate(TOPIC_NAME, 3)).thenReturn(generated);
        when(topicRepository.findByName(TOPIC_NAME)).thenReturn(Optional.of(existingTopic));
        when(vocabularyRepository.findWordsByTopicId(TOPIC_ID)).thenReturn(Set.of()); // nothing exists yet
        when(vocabularyRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        AiSyncRequest  request  = new AiSyncRequest(TOPIC_NAME, 3);
        AiSyncResponse response = service.sync(request);

        assertThat(response.topicId()).isEqualTo(TOPIC_ID);
        assertThat(response.topicName()).isEqualTo(TOPIC_NAME);
        assertThat(response.totalGenerated()).isEqualTo(3);
        assertThat(response.saved()).isEqualTo(3);
        assertThat(response.skippedDuplicates()).isEqualTo(0);

        // Topic must NOT be created again
        verify(topicRepository, never()).save(any(Topic.class));

        // saveAll must be called once with all 3 entities
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Vocabulary>> captor = ArgumentCaptor.forClass(List.class);
        verify(vocabularyRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(3);
    }

    // ── CREATE path ────────────────────────────────────────────────────────────

    @Test
    void sync_createsNewTopic_whenTopicDoesNotExist() {
        Topic savedTopic = spy(new Topic());
        savedTopic.setName(TOPIC_NAME);
        UUID newId = UUID.randomUUID();
        doReturn(newId).when(savedTopic).getId();

        List<HighVolumeWordGenerator.WordEntry> generated = List.of(wordEntry("lambda"));

        when(wordGenerator.generate(TOPIC_NAME, 1)).thenReturn(generated);
        when(topicRepository.findByName(TOPIC_NAME)).thenReturn(Optional.empty());
        when(topicRepository.save(any(Topic.class))).thenReturn(savedTopic);
        when(vocabularyRepository.findWordsByTopicId(newId)).thenReturn(Set.of());
        when(vocabularyRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        AiSyncResponse response = service.sync(new AiSyncRequest(TOPIC_NAME, 1));

        assertThat(response.topicId()).isEqualTo(newId);
        assertThat(response.saved()).isEqualTo(1);
        assertThat(response.skippedDuplicates()).isEqualTo(0);

        ArgumentCaptor<Topic> topicCaptor = ArgumentCaptor.forClass(Topic.class);
        verify(topicRepository).save(topicCaptor.capture());
        assertThat(topicCaptor.getValue().getName()).isEqualTo(TOPIC_NAME);
        assertThat(topicCaptor.getValue().getStatus()).isEqualTo(TopicStatus.ACTIVE);
    }

    // ── Deduplication ──────────────────────────────────────────────────────────

    @Test
    void sync_deduplicates_viaStreamFilter_notIndividualQueries() {
        // "algorithm" already exists (lowercase in the set)
        List<HighVolumeWordGenerator.WordEntry> generated = List.of(
                wordEntry("algorithm"),   // duplicate — must be skipped
                wordEntry("recursion"),   // new
                wordEntry("compiler")     // new
        );
        when(wordGenerator.generate(TOPIC_NAME, 3)).thenReturn(generated);
        when(topicRepository.findByName(TOPIC_NAME)).thenReturn(Optional.of(existingTopic));
        when(vocabularyRepository.findWordsByTopicId(TOPIC_ID)).thenReturn(Set.of("algorithm")); // pre-existing
        when(vocabularyRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        AiSyncResponse response = service.sync(new AiSyncRequest(TOPIC_NAME, 3));

        assertThat(response.saved()).isEqualTo(2);              // recursion + compiler
        assertThat(response.skippedDuplicates()).isEqualTo(1);  // algorithm

        // Dedup must use findWordsByTopicId — ONE query only (never existsByWordAndTopicId)
        verify(vocabularyRepository, times(1)).findWordsByTopicId(TOPIC_ID);
        verify(vocabularyRepository, never()).existsByWordAndTopicId(any(), any());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Vocabulary>> captor = ArgumentCaptor.forClass(List.class);
        verify(vocabularyRepository).saveAll(captor.capture());
        List<String> savedWords = captor.getValue().stream()
                .map(Vocabulary::getWord).toList();
        assertThat(savedWords).containsExactlyInAnyOrder("recursion", "compiler");
    }

    @Test
    void sync_callsSaveAllOnce_forAllNewWords() {
        // Verify batch approach: saveAll called exactly once, not N times
        List<HighVolumeWordGenerator.WordEntry> generated = List.of(
                wordEntry("heap"), wordEntry("stack"), wordEntry("queue"),
                wordEntry("graph"), wordEntry("tree")
        );
        when(wordGenerator.generate(TOPIC_NAME, 5)).thenReturn(generated);
        when(topicRepository.findByName(TOPIC_NAME)).thenReturn(Optional.of(existingTopic));
        when(vocabularyRepository.findWordsByTopicId(TOPIC_ID)).thenReturn(Set.of());
        when(vocabularyRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        service.sync(new AiSyncRequest(TOPIC_NAME, 5));

        // saveAll called once — NOT 5 × save()
        verify(vocabularyRepository, times(1)).saveAll(any());
        verify(vocabularyRepository, never()).save(any(Vocabulary.class));
    }

    @Test
    void sync_skipsAllWords_whenAllAreDuplicates() {
        List<HighVolumeWordGenerator.WordEntry> generated = List.of(wordEntry("heap"), wordEntry("stack"));
        when(wordGenerator.generate(TOPIC_NAME, 2)).thenReturn(generated);
        when(topicRepository.findByName(TOPIC_NAME)).thenReturn(Optional.of(existingTopic));
        when(vocabularyRepository.findWordsByTopicId(TOPIC_ID)).thenReturn(Set.of("heap", "stack"));

        AiSyncResponse response = service.sync(new AiSyncRequest(TOPIC_NAME, 2));

        assertThat(response.saved()).isEqualTo(0);
        assertThat(response.skippedDuplicates()).isEqualTo(2);
        verify(vocabularyRepository, never()).saveAll(any());
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static HighVolumeWordGenerator.WordEntry wordEntry(String word) {
        return new HighVolumeWordGenerator.WordEntry(word, "def of " + word, null, "noun", word + " example.", null, "MEDIUM");
    }
}
