package com.xanh.vocabulary.dto.response;

import java.util.List;

public record PomodoroSyncResponse(
        int created,
        int updated,
        int wordsAdded,
        List<TopicDto> topics
) {}
