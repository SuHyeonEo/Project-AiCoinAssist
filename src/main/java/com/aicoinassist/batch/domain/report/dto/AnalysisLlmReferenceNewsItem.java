package com.aicoinassist.batch.domain.report.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnalysisLlmReferenceNewsItem(
        String title,
        String source,
        Instant publishedAt,
        String url,
        String whyItMatters,
        String relatedDomain
) {
}
