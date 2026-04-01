package com.xanh.vocabulary.repository;

import com.xanh.vocabulary.entity.SchedulerConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SchedulerConfigRepository extends JpaRepository<SchedulerConfig, UUID> {
    Optional<SchedulerConfig> findByConfigKey(String configKey);
}
