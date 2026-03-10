package com.aicoinassist.batch.domain.report.dto;

public record AnalysisMarketContextPayload(
        AnalysisCurrentStatePayload currentState,
        AnalysisComparisonContextPayload comparisonContext,
        AnalysisWindowContextPayload windowContext,
        AnalysisLevelContextPayload levelContext,
        AnalysisDerivativeContextSummaryPayload derivativeContextSummary,
        AnalysisContextHeadlinePayload derivativeHeadline,
        AnalysisMacroContextSummaryPayload macroContextSummary,
        AnalysisContextHeadlinePayload macroHeadline,
        AnalysisSentimentContextSummaryPayload sentimentContextSummary,
        AnalysisContextHeadlinePayload sentimentHeadline,
        AnalysisContinuityContextPayload continuityContext
) {
}
