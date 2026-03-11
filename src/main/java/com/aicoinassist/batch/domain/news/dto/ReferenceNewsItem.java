package com.aicoinassist.batch.domain.news.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ReferenceNewsItem(
        ReferenceNewsCategory category,
        String title,
        String source,
        Instant publishedAt,
        String url,
        String selectionReason
) {
}
