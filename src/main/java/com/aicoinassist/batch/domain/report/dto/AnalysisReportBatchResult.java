package com.aicoinassist.batch.domain.report.dto;

public record AnalysisReportBatchResult(
        String symbol,
        int snapshotCount,
        int reportCount
) {
}
