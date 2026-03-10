package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeDirection;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeSeverity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeTransitionType;

import java.math.BigDecimal;
import java.time.Instant;

public record AnalysisExternalRegimeTransition(
        AnalysisComparisonReference reference,
        Instant referenceTime,
        AnalysisExternalRegimeTransitionType transitionType,
        AnalysisExternalRegimeDirection resultingDirection,
        AnalysisExternalRegimeSeverity resultingSeverity,
        BigDecimal compositeRiskScoreDelta,
        String summary
) {
}
