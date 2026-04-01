package com.xanh.vocabulary.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /api/v1/topics/sync-ai.
 *
 * @param topicName  the topic to create / update
 * @param wordCount  how many vocabulary words to generate (200 – 400)
 */
public record AiSyncRequest(

        @NotBlank(message = "topicName must not be blank")
        @Size(max = 100, message = "topicName must be ≤ 100 characters")
        String topicName,

        @Min(value = 1,   message = "wordCount must be at least 1")
        @Max(value = 400, message = "wordCount must be at most 400")
        int wordCount
) {
    /** Default constructor used when wordCount is omitted in JSON. */
    public AiSyncRequest {
        if (wordCount == 0) wordCount = 200;
    }
}
