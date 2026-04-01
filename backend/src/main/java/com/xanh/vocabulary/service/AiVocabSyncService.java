package com.xanh.vocabulary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI Vocabulary Sync Service — PLACEHOLDER IMPLEMENTATION
 *
 * <p>Current behaviour: delegates to the free dictionary API
 * ({@link ExternalVocabApiClient}) so the upsert pipeline works end-to-end
 * without a paid AI provider.
 *
 * <p>To integrate a real AI provider (OpenAI, Gemini, etc.) replace
 * {@link #analyze} with a call to your chosen API.  The rest of the
 * pipeline ({@link PomodoroSyncService}, controller, frontend) is
 * completely AI-provider-agnostic.
 *
 * <h3>Example future implementation:</h3>
 * <pre>{@code
 * public List<TopicSyncData> analyze(List<String> keywords) {
 *     String prompt = "Return 10 English vocabulary words for each topic: "
 *             + String.join(", ", keywords)
 *             + ". Respond in JSON: [{topicName, words:[{word,definition,...}]}]";
 *     String json = openAiClient.complete(prompt);
 *     return objectMapper.readValue(json, new TypeReference<>() {});
 * }
 * }</pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiVocabSyncService {

    private final ExternalVocabApiClient externalVocabApiClient;

    /**
     * Analyzes the given keywords and returns structured topic + vocabulary data.
     *
     * @param keywords list of topic keywords (e.g. ["technology", "science"])
     * @return list of {@link TopicSyncData} — one entry per keyword that
     *         returned at least one vocabulary result
     */
    public List<TopicSyncData> analyze(List<String> keywords) {
        return keywords.stream()
                .map(keyword -> {
                    String topicName = toTitleCase(keyword.trim());
                    List<ExternalVocabApiClient.WordEntry> words =
                            externalVocabApiClient.fetchWord(keyword.trim().toLowerCase());
                    log.debug("AI sync: keyword='{}' → {} word(s) fetched", keyword, words.size());
                    return new TopicSyncData(topicName, words);
                })
                .filter(data -> !data.words().isEmpty())
                .toList();
    }

    // ── Internal model ────────────────────────────────────────────────────────

    /**
     * Intermediate data structure carrying a resolved topic name and its
     * associated vocabulary entries before the upsert step.
     */
    public record TopicSyncData(
            String topicName,
            List<ExternalVocabApiClient.WordEntry> words
    ) {}

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;
        String[] words = input.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                  .append(w.substring(1).toLowerCase())
                  .append(' ');
            }
        }
        return sb.toString().trim();
    }
}
