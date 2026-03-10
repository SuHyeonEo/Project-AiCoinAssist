package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisLlmRiskScenarioInput(
        List<AnalysisRiskFactor> riskFactors,
        List<AnalysisScenario> scenarios,
        List<AnalysisContinuityNote> continuityNotes
) {
}
