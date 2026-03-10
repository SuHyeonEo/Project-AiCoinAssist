package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalysisExternalContextComparisonFact(
        AnalysisComparisonReference reference,
        Instant referenceTime,
        BigDecimal referenceCompositeRiskScore,
        BigDecimal compositeRiskScoreDelta,
        Boolean dominantDirectionChanged,
        Boolean highestSeverityChanged,
        Integer supportiveSignalCountDelta,
        Integer cautionarySignalCountDelta,
        Integer headwindSignalCountDelta,
        Boolean primarySignalChanged,
        String referencePrimarySignalTitle
) {
}
