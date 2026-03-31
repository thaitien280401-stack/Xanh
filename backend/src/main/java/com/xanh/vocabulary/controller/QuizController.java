package com.xanh.vocabulary.controller;

import com.xanh.vocabulary.dto.request.GenerateQuizRequest;
import com.xanh.vocabulary.dto.request.SubmitQuizRequest;
import com.xanh.vocabulary.dto.response.QuizDto;
import com.xanh.vocabulary.entity.User;
import com.xanh.vocabulary.repository.UserRepository;
import com.xanh.vocabulary.service.QuizService;
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
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quiz", description = "Quiz generation and submission")
@SecurityRequirement(name = "bearerAuth")
public class QuizController {

    private final QuizService quizService;
    private final UserRepository userRepository;

    @PostMapping("/generate")
    @Operation(summary = "Generate a new quiz for a topic")
    public ResponseEntity<QuizDto> generate(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody GenerateQuizRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quizService.generateQuiz(resolveUserId(userDetails), request));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit quiz answers")
    public ResponseEntity<QuizDto> submit(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody SubmitQuizRequest request) {
        return ResponseEntity.ok(quizService.submitQuiz(resolveUserId(userDetails), id, request));
    }

    @GetMapping
    @Operation(summary = "List user's quizzes")
    public ResponseEntity<Page<QuizDto>> list(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(quizService.getUserQuizzes(resolveUserId(userDetails), pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get quiz by ID")
    public ResponseEntity<QuizDto> getById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(quizService.getQuiz(resolveUserId(userDetails), id));
    }

    private UUID resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"))
                .getId();
    }
}
