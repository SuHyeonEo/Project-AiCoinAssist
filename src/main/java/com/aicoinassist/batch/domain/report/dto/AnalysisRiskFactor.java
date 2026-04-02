package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisRiskFactorType;

import java.util.List;

public record AnalysisRiskFactor(
        AnalysisRiskFactorType type,
        String title,
        String summary,
        List<String> triggerFacts
) {
}
