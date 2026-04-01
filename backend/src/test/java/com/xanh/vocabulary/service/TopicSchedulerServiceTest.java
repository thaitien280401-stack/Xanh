package com.xanh.vocabulary.service;

import com.xanh.vocabulary.entity.SchedulerConfig;
import com.xanh.vocabulary.entity.Topic;
import com.xanh.vocabulary.enums.TopicStatus;
import com.xanh.vocabulary.repository.SchedulerConfigRepository;
import com.xanh.vocabulary.repository.TopicRepository;
import com.xanh.vocabulary.repository.VocabularyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TopicSchedulerServiceTest {

    @Mock TopicRepository topicRepository;
    @Mock VocabularyRepository vocabularyRepository;
    @Mock SchedulerConfigRepository schedulerConfigRepository;

    @InjectMocks TopicSchedulerService schedulerService;

    private Topic activeTopic;

    @BeforeEach
    void setUp() {
        activeTopic = new Topic();
        activeTopic.setName("Technology");
        activeTopic.setStatus(TopicStatus.ACTIVE);
    }

    // ── chargeTopics() ────────────────────────────────────────────────────────

    @Test
    void chargeTopics_marksTopicDone_whenVocabCountMeetsThreshold() {
        stubThreshold(10L);
        when(topicRepository.findByStatus(TopicStatus.ACTIVE)).thenReturn(List.of(activeTopic));
        when(vocabularyRepository.countByTopicId(any())).thenReturn(10L);

        schedulerService.chargeTopics();

        assertThat(activeTopic.getStatus()).isEqualTo(TopicStatus.DONE);
        verify(topicRepository).save(activeTopic);
    }

    @Test
    void chargeTopics_doesNotMarkDone_whenVocabCountBelowThreshold() {
        stubThreshold(10L);
        when(topicRepository.findByStatus(TopicStatus.ACTIVE)).thenReturn(List.of(activeTopic));
        when(vocabularyRepository.countByTopicId(any())).thenReturn(5L);

        schedulerService.chargeTopics();

        assertThat(activeTopic.getStatus()).isEqualTo(TopicStatus.ACTIVE);
        verify(topicRepository, never()).save(any());
    }

    @Test
    void chargeTopics_processesOnlyActiveTopics() {
        stubThreshold(1L);
        when(topicRepository.findByStatus(TopicStatus.ACTIVE)).thenReturn(List.of(activeTopic));
        when(vocabularyRepository.countByTopicId(any())).thenReturn(5L);

        schedulerService.chargeTopics();

        // findByStatus called specifically with ACTIVE — not all topics
        verify(topicRepository).findByStatus(TopicStatus.ACTIVE);
        verify(topicRepository, never()).findAll();
    }

    @Test
    void chargeTopics_marksMultipleTopicsDone() {
        stubThreshold(3L);
        Topic t1 = topicWithStatus("Math", TopicStatus.ACTIVE);
        Topic t2 = topicWithStatus("Science", TopicStatus.ACTIVE);

        when(topicRepository.findByStatus(TopicStatus.ACTIVE)).thenReturn(List.of(t1, t2));
        when(vocabularyRepository.countByTopicId(any())).thenReturn(5L);

        schedulerService.chargeTopics();

        assertThat(t1.getStatus()).isEqualTo(TopicStatus.DONE);
        assertThat(t2.getStatus()).isEqualTo(TopicStatus.DONE);
        verify(topicRepository, times(2)).save(any(Topic.class));
    }

    @Test
    void chargeTopics_usesDefaultThreshold_whenConfigMissing() {
        when(schedulerConfigRepository.findByConfigKey(TopicSchedulerService.KEY_MIN_VOCAB_THRESHOLD))
                .thenReturn(Optional.empty());
        when(topicRepository.findByStatus(TopicStatus.ACTIVE)).thenReturn(List.of(activeTopic));
        when(vocabularyRepository.countByTopicId(any())).thenReturn(10L); // default = 10

        schedulerService.chargeTopics();

        assertThat(activeTopic.getStatus()).isEqualTo(TopicStatus.DONE);
    }

    // ── getIntervalHours() ────────────────────────────────────────────────────

    @Test
    void getIntervalHours_returnsStoredValue() {
        when(schedulerConfigRepository.findByConfigKey(TopicSchedulerService.KEY_INTERVAL_HOURS))
                .thenReturn(Optional.of(configEntry(TopicSchedulerService.KEY_INTERVAL_HOURS, "4")));

        assertThat(schedulerService.getIntervalHours()).isEqualTo(4L);
    }

    @Test
    void getIntervalHours_returnsDefault2_whenConfigMissing() {
        when(schedulerConfigRepository.findByConfigKey(TopicSchedulerService.KEY_INTERVAL_HOURS))
                .thenReturn(Optional.empty());

        assertThat(schedulerService.getIntervalHours()).isEqualTo(2L);
    }

    // ── isEnabled() ───────────────────────────────────────────────────────────

    @Test
    void isEnabled_returnsFalse_whenDisabledInConfig() {
        when(schedulerConfigRepository.findByConfigKey(TopicSchedulerService.KEY_SCHEDULER_ENABLED))
                .thenReturn(Optional.of(configEntry(TopicSchedulerService.KEY_SCHEDULER_ENABLED, "false")));

        assertThat(schedulerService.isEnabled()).isFalse();
    }

    @Test
    void isEnabled_returnsTrue_whenConfigMissing() {
        when(schedulerConfigRepository.findByConfigKey(TopicSchedulerService.KEY_SCHEDULER_ENABLED))
                .thenReturn(Optional.empty());

        assertThat(schedulerService.isEnabled()).isTrue();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void stubThreshold(long value) {
        when(schedulerConfigRepository.findByConfigKey(TopicSchedulerService.KEY_MIN_VOCAB_THRESHOLD))
                .thenReturn(Optional.of(configEntry(TopicSchedulerService.KEY_MIN_VOCAB_THRESHOLD, String.valueOf(value))));
    }

    private SchedulerConfig configEntry(String key, String value) {
        SchedulerConfig c = new SchedulerConfig();
        c.setConfigKey(key);
        c.setConfigValue(value);
        return c;
    }

    private Topic topicWithStatus(String name, TopicStatus status) {
        Topic t = new Topic();
        t.setName(name);
        t.setStatus(status);
        return t;
    }
}
