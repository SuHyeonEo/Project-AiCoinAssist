package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisReportPayload(
        String summary,
        String marketContext,
        List<AnalysisComparisonFact> comparisonFacts,
        List<AnalysisComparisonHighlight> comparisonHighlights,
        List<AnalysisWindowHighlight> windowHighlights,
        List<AnalysisContinuityNote> continuityNotes,
        List<AnalysisWindowSummary> windowSummaries,
        AnalysisDerivativeContext derivativeContext,
        List<AnalysisPriceLevel> supportLevels,
        List<AnalysisPriceLevel> resistanceLevels,
        List<AnalysisRiskFactor> riskFactors,
        List<AnalysisScenario> scenarios
) {
}
