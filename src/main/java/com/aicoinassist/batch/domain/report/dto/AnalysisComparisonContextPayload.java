package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisComparisonContextPayload(
        String factSummary,
        List<String> highlightDetails
) {
}
