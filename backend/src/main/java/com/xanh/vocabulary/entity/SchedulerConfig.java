package com.xanh.vocabulary.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scheduler_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchedulerConfig extends BaseEntity {

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "config_value", nullable = false, length = 255)
    private String configValue;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
