package com.aicoinassist.batch.domain.report.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnalysisLlmScenarioOutput(
        String scenarioType,
        String title,
        String condition,
        String trigger,
        String confirmation,
        String invalidation,
        String interpretation
) {
}
