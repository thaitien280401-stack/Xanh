package com.xanh.vocabulary.controller;

import com.xanh.vocabulary.dto.request.PomodoroSyncRequest;
import com.xanh.vocabulary.dto.response.PomodoroSyncResponse;
import com.xanh.vocabulary.service.PomodoroSyncService;
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
public class PomodoroSyncController {

    private final PomodoroSyncService pomodoroSyncService;

    @PostMapping("/pomodoro-sync")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Pomodoro Topic Sync (Upsert)",
        description = "Calls the AI/Vocabulary API for each keyword, then upserts topics: " +
                      "updates existing ones with new words, creates new ones if absent."
    )
    public ResponseEntity<PomodoroSyncResponse> pomodoroSync(
            @Valid @RequestBody PomodoroSyncRequest request) {
        return ResponseEntity.ok(pomodoroSyncService.sync(request.keywords()));
    }
}
