package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisScenarioBias;

public record AnalysisScenario(
        String title,
        AnalysisScenarioBias bias,
        String description
) {
}
