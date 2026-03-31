package com.xanh.vocabulary.controller;

import com.xanh.vocabulary.dto.response.ProgressStatsDto;
import com.xanh.vocabulary.repository.UserRepository;
import com.xanh.vocabulary.service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
@Tag(name = "Progress", description = "User learning progress and statistics")
@SecurityRequirement(name = "bearerAuth")
public class ProgressController {

    private final ProgressService progressService;
    private final UserRepository userRepository;

    @GetMapping("/stats")
    @Operation(summary = "Get user's overall learning statistics")
    public ResponseEntity<ProgressStatsDto> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"))
                .getId();
        return ResponseEntity.ok(progressService.getStats(userId));
    }
}
