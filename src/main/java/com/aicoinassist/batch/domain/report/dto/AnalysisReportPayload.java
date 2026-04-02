package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisReportPayload(
        AnalysisSummaryPayload summary,
        AnalysisMarketContextPayload marketContext,
        List<AnalysisComparisonFact> comparisonFacts,
        List<AnalysisComparisonHighlight> comparisonHighlights,
        List<AnalysisWindowHighlight> windowHighlights,
        List<AnalysisContinuityNote> continuityNotes,
        List<AnalysisWindowSummary> windowSummaries,
        List<String> marketParticipationFacts,
        List<AnalysisMarketParticipationSummary> marketParticipationSummaries,
        AnalysisDerivativeContext derivativeContext,
        AnalysisMacroContext macroContext,
        AnalysisSentimentContext sentimentContext,
        AnalysisOnchainContext onchainContext,
        List<AnalysisPriceLevel> supportLevels,
        List<AnalysisPriceLevel> resistanceLevels,
        List<AnalysisPriceZone> supportZones,
        List<AnalysisPriceZone> resistanceZones,
        AnalysisPriceZone nearestSupportZone,
        AnalysisPriceZone nearestResistanceZone,
        List<AnalysisZoneInteractionFact> zoneInteractionFacts,
        List<AnalysisRiskFactor> riskFactors,
        List<AnalysisScenario> scenarios
) {
}
