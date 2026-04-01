package com.xanh.vocabulary.service;

import com.xanh.vocabulary.entity.SchedulerConfig;
import com.xanh.vocabulary.entity.Topic;
import com.xanh.vocabulary.enums.TopicStatus;
import com.xanh.vocabulary.repository.SchedulerConfigRepository;
import com.xanh.vocabulary.repository.TopicRepository;
import com.xanh.vocabulary.repository.VocabularyRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicSchedulerService {

    public static final String KEY_INTERVAL_HOURS      = "INTERVAL_HOURS";
    public static final String KEY_MIN_VOCAB_THRESHOLD = "MIN_VOCAB_THRESHOLD";
    public static final String KEY_SCHEDULER_ENABLED   = "SCHEDULER_ENABLED";

    private final TopicRepository topicRepository;
    private final VocabularyRepository vocabularyRepository;
    private final SchedulerConfigRepository schedulerConfigRepository;

    private final ThreadPoolTaskScheduler taskScheduler = buildTaskScheduler();
    private ScheduledFuture<?> currentTask;

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @PostConstruct
    public void init() {
        reschedule();
    }

    @PreDestroy
    public void shutdown() {
        cancelCurrent();
        taskScheduler.shutdown();
    }

    // ── Public API (called by AdminSchedulerController) ───────────────────────

    public void reschedule() {
        if (!isEnabled()) {
            log.info("Topic scheduler is disabled — skipping schedule.");
            cancelCurrent();
            return;
        }
        long intervalMs = getIntervalHours() * 3_600_000L;
        cancelCurrent();
        currentTask = taskScheduler.scheduleAtFixedRate(this::chargeTopics, Instant.now(), Duration.ofMillis(intervalMs));
        log.info("Topic scheduler (re)scheduled — interval: {} hour(s)", getIntervalHours());
    }

    // ── Core task ─────────────────────────────────────────────────────────────

    @Transactional
    public void chargeTopics() {
        long threshold = getMinVocabThreshold();
        List<Topic> activeTopics = topicRepository.findByStatus(TopicStatus.ACTIVE);

        int markedDone = 0;
        for (Topic topic : activeTopics) {
            long count = vocabularyRepository.countByTopicId(topic.getId());
            if (count >= threshold) {
                topic.setStatus(TopicStatus.DONE);
                topicRepository.save(topic);
                markedDone++;
                log.info("Topic '{}' marked DONE (vocab count: {}/{})", topic.getName(), count, threshold);
            }
        }
        log.debug("Scheduler run complete — checked: {}, marked DONE: {}", activeTopics.size(), markedDone);
    }

    // ── Config helpers ────────────────────────────────────────────────────────

    public long getIntervalHours() {
        return getLongConfig(KEY_INTERVAL_HOURS, 2L);
    }

    public long getMinVocabThreshold() {
        return getLongConfig(KEY_MIN_VOCAB_THRESHOLD, 10L);
    }

    public boolean isEnabled() {
        return getBoolConfig(KEY_SCHEDULER_ENABLED, true);
    }

    private long getLongConfig(String key, long defaultValue) {
        return schedulerConfigRepository.findByConfigKey(key)
                .map(c -> {
                    try { return Long.parseLong(c.getConfigValue()); }
                    catch (NumberFormatException e) { return defaultValue; }
                })
                .orElse(defaultValue);
    }

    private boolean getBoolConfig(String key, boolean defaultValue) {
        return schedulerConfigRepository.findByConfigKey(key)
                .map(c -> Boolean.parseBoolean(c.getConfigValue()))
                .orElse(defaultValue);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private void cancelCurrent() {
        if (currentTask != null && !currentTask.isCancelled()) {
            currentTask.cancel(false);
        }
    }

    private static ThreadPoolTaskScheduler buildTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("topic-scheduler-");
        scheduler.initialize();
        return scheduler;
    }
}
