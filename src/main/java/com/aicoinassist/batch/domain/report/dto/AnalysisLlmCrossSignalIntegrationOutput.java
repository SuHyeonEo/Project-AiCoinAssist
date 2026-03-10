package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisLlmCrossSignalIntegrationOutput(
        List<String> alignedSignals,
        List<String> conflictingSignals,
        List<String> dominantDrivers,
        String combinedStructure
) {
}
