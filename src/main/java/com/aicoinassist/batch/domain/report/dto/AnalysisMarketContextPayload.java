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
        AnalysisOnchainContextSummaryPayload onchainContextSummary,
        AnalysisContextHeadlinePayload onchainHeadline,
        AnalysisContextHeadlinePayload externalHeadline,
        AnalysisExternalContextCompositePayload externalContextComposite,
        AnalysisContinuityContextPayload continuityContext,
        java.util.List<AnalysisExternalRegimeSignal> externalRegimeSignals
) {
}
