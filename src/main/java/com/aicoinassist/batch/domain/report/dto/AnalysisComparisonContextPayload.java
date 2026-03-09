package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisComparisonContextPayload(
        AnalysisContextHeadlinePayload headline,
        String factSummary,
        List<String> highlightDetails
) {
}
