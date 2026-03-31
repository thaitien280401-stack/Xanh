package com.xanh.vocabulary.controller;

import com.xanh.vocabulary.dto.response.VocabularyDto;
import com.xanh.vocabulary.service.VocabularyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vocabularies")
@RequiredArgsConstructor
@Tag(name = "Vocabularies", description = "Vocabulary CRUD and import")
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping
    @Operation(summary = "List vocabularies by topic")
    public ResponseEntity<Page<VocabularyDto>> getByTopic(
            @RequestParam UUID topicId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(vocabularyService.getByTopic(topicId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vocabulary by ID")
    public ResponseEntity<VocabularyDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(vocabularyService.getById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Full-text search across vocabularies")
    public ResponseEntity<Page<VocabularyDto>> search(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(vocabularyService.search(q, pageable));
    }

    @PostMapping("/import")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Import a word from the external API into a topic")
    public ResponseEntity<List<VocabularyDto>> importWord(
            @RequestParam UUID topicId,
            @RequestParam String word) {
        return ResponseEntity.ok(vocabularyService.importFromExternalApi(topicId, word));
    }
}
