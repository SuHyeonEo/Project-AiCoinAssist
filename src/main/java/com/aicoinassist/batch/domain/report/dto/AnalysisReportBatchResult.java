package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionStatus;

import java.time.Instant;
import java.util.List;

public record AnalysisReportBatchResult(
        String runId,
        String symbol,
        Instant startedAt,
        Instant finishedAt,
        long durationMillis,
        List<AnalysisReportSnapshotStepResult> snapshotResults,
        List<AnalysisReportStepResult> reportResults,
        String crashErrorMessage
) {

    public AnalysisReportBatchResult {
        snapshotResults = List.copyOf(snapshotResults);
        reportResults = List.copyOf(reportResults);
    }

    public static AnalysisReportBatchResult crashed(
            String runId,
            String symbol,
            Instant startedAt,
            Instant finishedAt,
            String crashErrorMessage
    ) {
        return new AnalysisReportBatchResult(
                runId,
                symbol,
                startedAt,
                finishedAt,
                finishedAt.toEpochMilli() - startedAt.toEpochMilli(),
                List.of(),
                List.of(),
                crashErrorMessage
        );
    }

    public int snapshotSuccessCount() {
        return (int) snapshotResults.stream()
                                    .filter(AnalysisReportSnapshotStepResult::success)
                                    .count();
    }

    public int reportSuccessCount() {
        return (int) reportResults.stream()
                                  .filter(AnalysisReportStepResult::success)
                                  .count();
    }

    public int snapshotFailureCount() {
        return snapshotResults.size() - snapshotSuccessCount();
    }

    public int reportFailureCount() {
        return reportResults.size() - reportSuccessCount();
    }

    public boolean hasFailures() {
        return status() != BatchExecutionStatus.SUCCESS;
    }

    public boolean crashed() {
        return crashErrorMessage != null && !crashErrorMessage.isBlank();
    }

    public BatchExecutionStatus status() {
        if (crashed()) {
            return BatchExecutionStatus.FAILED;
        }
        if (snapshotFailureCount() > 0 || reportFailureCount() > 0) {
            return BatchExecutionStatus.PARTIAL_FAILURE;
        }
        return BatchExecutionStatus.SUCCESS;
    }
}
