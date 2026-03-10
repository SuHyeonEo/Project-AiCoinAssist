package com.aicoinassist.batch.domain.report.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnalysisLlmDomainAnalysisOutput(
        String domain,
        String currentSignal,
        List<String> keyFacts,
        String interpretation,
        String pressure,
        String confidence,
        List<String> caveats
) {
}
