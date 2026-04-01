package com.xanh.vocabulary.controller;

import com.xanh.vocabulary.dto.request.AiSyncRequest;
import com.xanh.vocabulary.dto.response.AiSyncResponse;
import com.xanh.vocabulary.service.AiHighVolumeSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
@Tag(name = "Topics", description = "Vocabulary topic management")
public class AiSyncController {

    private final AiHighVolumeSyncService aiHighVolumeSyncService;

    @PostMapping("/sync-ai")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "High-Volume AI Vocabulary Sync",
        description = "Generates 1–400 vocabulary words for a topic using the AI placeholder, " +
                      "then batch-upserts them (saveAll + stream deduplication). " +
                      "Suitable for seeding large word lists in a single request."
    )
    public ResponseEntity<AiSyncResponse> syncAi(@Valid @RequestBody AiSyncRequest request) {
        return ResponseEntity.ok(aiHighVolumeSyncService.sync(request));
    }
}
