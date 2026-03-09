package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelLabel;

import java.math.BigDecimal;

public record AnalysisPriceLevel(
        AnalysisPriceLevelLabel label,
        BigDecimal price,
        String rationale
) {
}
