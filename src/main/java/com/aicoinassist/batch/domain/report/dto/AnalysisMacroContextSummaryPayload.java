package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisMacroContextSummaryPayload(
        String currentStateSummary,
        String comparisonSummary,
        List<String> highlightDetails
) {
}
