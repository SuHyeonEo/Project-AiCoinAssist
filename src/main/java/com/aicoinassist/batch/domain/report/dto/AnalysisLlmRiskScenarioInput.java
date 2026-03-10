package com.aicoinassist.batch.domain.report.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnalysisLlmRiskScenarioInput(
        List<AnalysisRiskFactor> riskFactors,
        List<AnalysisScenario> scenarios,
        List<AnalysisContinuityNote> continuityNotes
) {
}
