package com.aicoinassist.batch.domain.report.dto;

import java.time.Instant;

public record AnalysisLlmReferenceNewsItem(
        String title,
        String source,
        Instant publishedAt,
        String url,
        String whyItMatters,
        String relatedDomain
) {
}
