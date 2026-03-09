package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelSourceType;

import java.math.BigDecimal;
import java.util.List;

public record AnalysisPriceLevel(
        AnalysisPriceLevelLabel label,
        AnalysisPriceLevelSourceType sourceType,
        BigDecimal price,
        BigDecimal distanceFromCurrent,
        BigDecimal strengthScore,
        String rationale,
        List<String> triggerFacts
) {
}
