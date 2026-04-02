package com.aicoinassist.batch.domain.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record AnalysisLevelContextPayload(
        AnalysisPriceZone nearestSupportZone,
        AnalysisPriceZone nearestResistanceZone,
        List<AnalysisZoneInteractionFact> zoneInteractionFacts,
        BigDecimal supportBreakRisk,
        BigDecimal resistanceBreakRisk,
        List<AnalysisLevelContextComparisonFact> comparisonFacts,
        List<AnalysisLevelContextHighlight> highlights
) {
}
