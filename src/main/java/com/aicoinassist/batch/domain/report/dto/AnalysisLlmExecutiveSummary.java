package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisConfidenceLevel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisOutlookType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnalysisLlmExecutiveSummary(
        String headline,
        AnalysisOutlookType outlook,
        AnalysisConfidenceLevel confidence,
        String primaryMessage
) {
}
