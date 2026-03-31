package com.xanh.vocabulary.service;

import com.xanh.vocabulary.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalVocabApiClient {

    private final AppProperties appProperties;
    private final RestClient restClient = RestClient.create();

    @SuppressWarnings("unchecked")
    public List<WordEntry> fetchWord(String word) {
        try {
            String url = appProperties.getExternalApi().getVocabBaseUrl() + "/" + word;
            List<Map<String, Object>> response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(List.class);

            if (response == null || response.isEmpty()) return List.of();
            return parseEntries(response);
        } catch (Exception e) {
            log.warn("Failed to fetch word '{}' from external API: {}", word, e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private List<WordEntry> parseEntries(List<Map<String, Object>> raw) {
        List<WordEntry> entries = new ArrayList<>();
        for (Map<String, Object> entry : raw) {
            String word = (String) entry.get("word");
            String phonetic = (String) entry.getOrDefault("phonetic", "");
            List<Map<String, Object>> meanings = (List<Map<String, Object>>) entry.getOrDefault("meanings", List.of());
            List<Map<String, Object>> phonetics = (List<Map<String, Object>>) entry.getOrDefault("phonetics", List.of());

            String audioUrl = phonetics.stream()
                    .filter(p -> p.get("audio") != null && !((String) p.get("audio")).isEmpty())
                    .map(p -> (String) p.get("audio"))
                    .findFirst().orElse(null);

            for (Map<String, Object> meaning : meanings) {
                String partOfSpeech = (String) meaning.getOrDefault("partOfSpeech", "");
                List<Map<String, Object>> defs = (List<Map<String, Object>>) meaning.getOrDefault("definitions", List.of());
                for (Map<String, Object> def : defs) {
                    String definition = (String) def.getOrDefault("definition", "");
                    String example = (String) def.getOrDefault("example", "");
                    entries.add(new WordEntry(word, definition, phonetic, partOfSpeech, example, audioUrl));
                    break; // one definition per part of speech
                }
            }
        }
        return entries;
    }

    public record WordEntry(
            String word,
            String definition,
            String pronunciation,
            String partOfSpeech,
            String exampleSentence,
            String audioUrl
    ) {}
}
