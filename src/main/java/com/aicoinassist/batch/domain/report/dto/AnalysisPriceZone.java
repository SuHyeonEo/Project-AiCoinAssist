package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelSourceType;

import java.math.BigDecimal;
import java.util.List;

public record AnalysisPriceZone(
        Integer zoneRank,
        BigDecimal representativePrice,
        BigDecimal zoneLow,
        BigDecimal zoneHigh,
        BigDecimal distanceFromCurrent,
        BigDecimal strengthScore,
        AnalysisPriceLevelLabel strongestLevelLabel,
        AnalysisPriceLevelSourceType strongestSourceType,
        Integer levelCount,
        List<AnalysisPriceLevelLabel> includedLevelLabels,
        List<AnalysisPriceLevelSourceType> includedSourceTypes,
        List<String> triggerFacts
) {
}
