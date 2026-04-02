package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisComparisonFactSummaryPayload(
        String primaryFact,
        List<String> referenceBreakdown
) {
}
