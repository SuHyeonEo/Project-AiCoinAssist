package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisRiskFactorType;

public record AnalysisRiskFactor(
        AnalysisRiskFactorType type,
        String title,
        String description
) {
}
