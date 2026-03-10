package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisLlmScenarioOutput(
        String scenarioType,
        String condition,
        List<String> triggers,
        List<String> confirmingSignals,
        List<String> invalidationSignals,
        String interpretation
) {
}
