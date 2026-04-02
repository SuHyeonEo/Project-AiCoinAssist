package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelSourceType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneInteractionType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneType;

import java.math.BigDecimal;
import java.util.List;

public record AnalysisPriceZone(
        AnalysisPriceZoneType zoneType,
        Integer zoneRank,
        BigDecimal representativePrice,
        BigDecimal zoneLow,
        BigDecimal zoneHigh,
        BigDecimal distanceFromCurrent,
        BigDecimal distanceToZone,
        BigDecimal strengthScore,
        AnalysisPriceZoneInteractionType interactionType,
        AnalysisPriceLevelLabel strongestLevelLabel,
        AnalysisPriceLevelSourceType strongestSourceType,
        Integer levelCount,
        Integer recentTestCount,
        Integer recentRejectionCount,
        Integer recentBreakCount,
        List<AnalysisPriceLevelLabel> includedLevelLabels,
        List<AnalysisPriceLevelSourceType> includedSourceTypes,
        List<String> triggerFacts
) {
}
