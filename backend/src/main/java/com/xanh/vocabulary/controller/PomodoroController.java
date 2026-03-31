package com.xanh.vocabulary.controller;

import com.xanh.vocabulary.dto.request.StartSessionRequest;
import com.xanh.vocabulary.dto.response.PomodoroSessionDto;
import com.xanh.vocabulary.entity.User;
import com.xanh.vocabulary.repository.UserRepository;
import com.xanh.vocabulary.service.PomodoroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pomodoro")
@RequiredArgsConstructor
@Tag(name = "Pomodoro", description = "Pomodoro session management")
@SecurityRequirement(name = "bearerAuth")
public class PomodoroController {

    private final PomodoroService pomodoroService;
    private final UserRepository userRepository;

    @PostMapping("/sessions")
    @Operation(summary = "Start a new Pomodoro session")
    public ResponseEntity<PomodoroSessionDto> start(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StartSessionRequest request) {
        UUID userId = resolveUserId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(pomodoroService.startSession(userId, request));
    }

    @GetMapping("/sessions")
    @Operation(summary = "List user's Pomodoro sessions")
    public ResponseEntity<Page<PomodoroSessionDto>> getSessions(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(pomodoroService.getSessions(resolveUserId(userDetails), pageable));
    }

    @GetMapping("/sessions/{id}")
    @Operation(summary = "Get session by ID")
    public ResponseEntity<PomodoroSessionDto> getSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(pomodoroService.getSession(resolveUserId(userDetails), id));
    }

    @PutMapping("/sessions/{id}/complete")
    @Operation(summary = "Mark a session as completed")
    public ResponseEntity<PomodoroSessionDto> complete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestParam(required = false) Integer wordsStudied) {
        return ResponseEntity.ok(pomodoroService.completeSession(resolveUserId(userDetails), id, wordsStudied));
    }

    @PutMapping("/sessions/{id}/abandon")
    @Operation(summary = "Abandon a session")
    public ResponseEntity<PomodoroSessionDto> abandon(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(pomodoroService.abandonSession(resolveUserId(userDetails), id));
    }

    private UUID resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"))
                .getId();
    }
}
