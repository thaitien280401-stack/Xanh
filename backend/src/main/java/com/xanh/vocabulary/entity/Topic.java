package com.xanh.vocabulary.entity;

import com.xanh.vocabulary.enums.TopicStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "topics", indexes = {
        @Index(name = "idx_topics_name", columnList = "name"),
        @Index(name = "idx_topics_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topic extends BaseEntity {

    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "external_api_ref", length = 255)
    private String externalApiRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TopicStatus status = TopicStatus.ACTIVE;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Vocabulary> vocabularies = new ArrayList<>();
}
