package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeDirection;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeSeverity;

import java.math.BigDecimal;

public record AnalysisExternalRegimeStatePayload(
        AnalysisExternalRegimeDirection dominantDirection,
        AnalysisExternalRegimeSeverity highestSeverity,
        AnalysisExternalRegimeCategory primarySignalCategory,
        String primarySignalTitle,
        BigDecimal compositeRiskScore,
        BigDecimal reversalRiskScore,
        String summary
) {
}
