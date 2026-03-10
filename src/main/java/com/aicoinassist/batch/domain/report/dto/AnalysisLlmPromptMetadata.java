package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnalysisLlmPromptMetadata(
        String symbol,
        AnalysisReportType reportType,
        Instant analysisBasisTime,
        Instant rawReferenceTime,
        String sourceDataVersion,
        String analysisEngineVersion
) {
}
