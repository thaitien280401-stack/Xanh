package com.xanh.vocabulary.controller;

import com.xanh.vocabulary.dto.request.CreateTopicRequest;
import com.xanh.vocabulary.dto.response.TopicDto;
import com.xanh.vocabulary.enums.TopicStatus;
import com.xanh.vocabulary.service.ExternalVocabApiClient;
import com.xanh.vocabulary.service.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
@Tag(name = "Topics", description = "Vocabulary topic management")
public class TopicController {

    private final TopicService topicService;
    private final ExternalVocabApiClient externalApi;

    @GetMapping
    @Operation(summary = "List all topics")
    public ResponseEntity<List<TopicDto>> getAll() {
        return ResponseEntity.ok(topicService.getAll());
    }

    @GetMapping("/paged")
    @Operation(summary = "Paginated topics filtered by status (18 per page)")
    public ResponseEntity<Page<TopicDto>> getPaged(
            @RequestParam(defaultValue = "ACTIVE") TopicStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "18") int size) {
        return ResponseEntity.ok(topicService.getPaged(status, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get topic by ID")
    public ResponseEntity<TopicDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(topicService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new topic (admin only)")
    public ResponseEntity<TopicDto> create(@Valid @RequestBody CreateTopicRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(topicService.create(request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Manually update a topic's status (admin only)")
    public ResponseEntity<TopicDto> updateStatus(
            @PathVariable UUID id,
            @RequestParam TopicStatus status) {
        return ResponseEntity.ok(topicService.updateStatus(id, status));
    }

    @GetMapping("/external/search")
    @Operation(summary = "Look up a word from the external dictionary API")
    public ResponseEntity<List<ExternalVocabApiClient.WordEntry>> searchExternal(
            @RequestParam String query) {
        return ResponseEntity.ok(externalApi.fetchWord(query));
    }
}
