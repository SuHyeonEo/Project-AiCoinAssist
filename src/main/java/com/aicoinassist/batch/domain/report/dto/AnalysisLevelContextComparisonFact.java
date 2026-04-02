package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneInteractionType;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalysisLevelContextComparisonFact(
        AnalysisComparisonReference reference,
        Instant referenceTime,
        BigDecimal supportRepresentativePriceChangeRate,
        BigDecimal resistanceRepresentativePriceChangeRate,
        BigDecimal supportStrengthDelta,
        BigDecimal resistanceStrengthDelta,
        BigDecimal supportBreakRiskDelta,
        BigDecimal resistanceBreakRiskDelta,
        AnalysisPriceZoneInteractionType currentSupportInteractionType,
        AnalysisPriceZoneInteractionType referenceSupportInteractionType,
        AnalysisPriceZoneInteractionType currentResistanceInteractionType,
        AnalysisPriceZoneInteractionType referenceResistanceInteractionType
) {
}
