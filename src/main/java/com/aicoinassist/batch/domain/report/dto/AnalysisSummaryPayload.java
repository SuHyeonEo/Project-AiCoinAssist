package com.aicoinassist.batch.domain.report.dto;

public record AnalysisSummaryPayload(
        String headline,
        String outlook,
        String confidence,
        String keyMessage,
        java.util.List<AnalysisContextHeadlinePayload> signalHeadlines
) {
}
