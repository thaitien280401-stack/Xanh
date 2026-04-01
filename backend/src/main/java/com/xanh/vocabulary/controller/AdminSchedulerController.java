package com.xanh.vocabulary.controller;

import com.xanh.vocabulary.dto.request.UpdateSchedulerConfigRequest;
import com.xanh.vocabulary.dto.response.SchedulerConfigDto;
import com.xanh.vocabulary.entity.SchedulerConfig;
import com.xanh.vocabulary.repository.SchedulerConfigRepository;
import com.xanh.vocabulary.service.TopicSchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/scheduler")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin — Scheduler", description = "Dynamic scheduler configuration (admin only)")
public class AdminSchedulerController {

    private final SchedulerConfigRepository schedulerConfigRepository;
    private final TopicSchedulerService schedulerService;

    @GetMapping("/config")
    @Operation(summary = "List all scheduler config entries")
    public ResponseEntity<List<SchedulerConfigDto>> getAll() {
        List<SchedulerConfigDto> list = schedulerConfigRepository.findAll()
                .stream().map(this::toDto).toList();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/config/{key}")
    @Operation(summary = "Update a scheduler config value; updating INTERVAL_HOURS reschedules immediately")
    public ResponseEntity<SchedulerConfigDto> update(
            @PathVariable String key,
            @Valid @RequestBody UpdateSchedulerConfigRequest request) {

        SchedulerConfig config = schedulerConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new EntityNotFoundException("Config key not found: " + key));

        config.setConfigValue(request.value());
        schedulerConfigRepository.save(config);

        if (TopicSchedulerService.KEY_INTERVAL_HOURS.equals(key)
                || TopicSchedulerService.KEY_SCHEDULER_ENABLED.equals(key)) {
            schedulerService.reschedule();
        }

        return ResponseEntity.ok(toDto(config));
    }

    private SchedulerConfigDto toDto(SchedulerConfig c) {
        return new SchedulerConfigDto(c.getId(), c.getConfigKey(), c.getConfigValue(),
                c.getDescription(), c.getUpdatedAt());
    }
}
