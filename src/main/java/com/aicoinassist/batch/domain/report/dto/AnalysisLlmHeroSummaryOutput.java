package com.aicoinassist.batch.domain.report.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnalysisLlmHeroSummaryOutput(
        String marketRegime,
        String oneLineTake,
        String primaryDriver,
        String riskDriver
) {
}
