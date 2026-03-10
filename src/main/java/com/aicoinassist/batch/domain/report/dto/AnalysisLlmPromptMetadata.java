package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;

import java.time.Instant;

public record AnalysisLlmPromptMetadata(
        String symbol,
        AnalysisReportType reportType,
        Instant analysisBasisTime,
        Instant rawReferenceTime,
        String sourceDataVersion,
        String analysisEngineVersion
) {
}
