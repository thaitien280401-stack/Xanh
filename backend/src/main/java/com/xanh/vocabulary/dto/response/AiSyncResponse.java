package com.xanh.vocabulary.dto.response;

import java.util.UUID;

/**
 * Response body for POST /api/v1/topics/sync-ai.
 */
public record AiSyncResponse(
        UUID   topicId,
        String topicName,
        int    totalGenerated,
        int    saved,
        int    skippedDuplicates,
        long   processingTimeMs
) {}
