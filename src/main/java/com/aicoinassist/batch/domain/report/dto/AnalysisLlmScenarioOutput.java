package com.aicoinassist.batch.domain.report.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnalysisLlmScenarioOutput(
        String scenarioType,
        String condition,
        List<String> triggers,
        List<String> confirmingSignals,
        List<String> invalidationSignals,
        String interpretation
) {
}
