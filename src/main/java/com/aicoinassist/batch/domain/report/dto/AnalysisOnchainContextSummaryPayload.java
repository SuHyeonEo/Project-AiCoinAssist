package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisOnchainContextSummaryPayload(
        String currentStateSummary,
        String comparisonSummary,
        String windowSummary,
        List<String> highlightDetails
) {
}
