package com.aicoinassist.batch.domain.report.dto;

public record AnalysisMarketContextPayload(
        AnalysisCurrentStatePayload currentState,
        AnalysisComparisonContextPayload comparisonContext,
        AnalysisWindowContextPayload windowContext,
        String derivativeContextSummary,
        AnalysisContextHeadlinePayload derivativeHeadline,
        AnalysisContinuityContextPayload continuityContext
) {
}
