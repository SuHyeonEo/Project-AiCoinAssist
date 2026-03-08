package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionStatus;

import java.time.Instant;
import java.util.List;

public record AnalysisReportBatchAssetResultView(
        String symbol,
        BatchExecutionStatus executionStatus,
        Instant startedAt,
        Instant finishedAt,
        long durationMillis,
        int snapshotSuccessCount,
        int snapshotFailureCount,
        int reportSuccessCount,
        int reportFailureCount,
        String crashErrorMessage,
        List<AnalysisReportSnapshotStepResult> snapshotResults,
        List<AnalysisReportStepResult> reportResults
) {
}
