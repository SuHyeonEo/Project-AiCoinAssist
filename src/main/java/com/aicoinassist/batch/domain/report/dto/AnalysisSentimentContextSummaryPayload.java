package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisSentimentContextSummaryPayload(
        String currentStateSummary,
        String comparisonSummary,
        String windowSummary,
        List<String> highlightDetails,
        Long nextUpdateHours
) {
}
