package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalysisSentimentComparisonFact(
        AnalysisComparisonReference reference,
        Instant referenceTime,
        BigDecimal referenceIndexValue,
        String referenceClassification,
        BigDecimal valueChange,
        BigDecimal valueChangeRate,
        Boolean classificationChanged
) {
}
