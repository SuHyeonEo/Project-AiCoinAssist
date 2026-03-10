package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalysisMacroComparisonFact(
        AnalysisComparisonReference reference,
        Instant referenceTime,
        BigDecimal referenceDxyProxyValue,
        BigDecimal referenceUs10yYieldValue,
        BigDecimal referenceUsdKrwValue,
        BigDecimal dxyProxyValueChange,
        BigDecimal dxyProxyChangeRate,
        BigDecimal us10yYieldValueChange,
        BigDecimal us10yYieldChangeRate,
        BigDecimal usdKrwValueChange,
        BigDecimal usdKrwChangeRate
) {
}
