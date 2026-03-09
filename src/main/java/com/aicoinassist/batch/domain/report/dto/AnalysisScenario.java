package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisScenarioBias;

import java.util.List;

public record AnalysisScenario(
        String title,
        AnalysisScenarioBias bias,
        List<String> triggerConditions,
        String pathSummary,
        List<String> invalidationSignals
) {
}
