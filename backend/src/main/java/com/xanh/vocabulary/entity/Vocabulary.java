package com.xanh.vocabulary.entity;

import com.xanh.vocabulary.enums.Difficulty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vocabularies", indexes = {
        @Index(name = "idx_vocabularies_topic_id", columnList = "topic_id"),
        @Index(name = "idx_vocabularies_word", columnList = "word")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vocabulary extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "word", nullable = false, length = 255)
    private String word;

    @Column(name = "definition", columnDefinition = "TEXT")
    private String definition;

    @Column(name = "pronunciation", length = 255)
    private String pronunciation;

    @Column(name = "part_of_speech", length = 50)
    private String partOfSpeech;

    @Column(name = "example_sentence", columnDefinition = "TEXT")
    private String exampleSentence;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", length = 10)
    @Builder.Default
    private Difficulty difficulty = Difficulty.MEDIUM;
}
