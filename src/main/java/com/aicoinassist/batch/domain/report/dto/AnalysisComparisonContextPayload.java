package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisComparisonContextPayload(
        AnalysisContextHeadlinePayload headline,
        AnalysisComparisonFactSummaryPayload factSummary,
        List<String> highlightDetails
) {
}
