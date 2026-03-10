package com.aicoinassist.batch.domain.report.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnalysisLlmCrossSignalIntegrationOutput(
        List<String> alignedSignals,
        List<String> conflictingSignals,
        List<String> dominantDrivers,
        String combinedStructure
) {
}
