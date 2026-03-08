package com.aicoinassist.batch.domain.report.dto;

import java.time.Instant;
import java.util.List;

public record AnalysisReportBatchResult(
        String runId,
        String symbol,
        Instant startedAt,
        Instant finishedAt,
        long durationMillis,
        List<AnalysisReportSnapshotStepResult> snapshotResults,
        List<AnalysisReportStepResult> reportResults
) {

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
        return snapshotFailureCount() > 0 || reportFailureCount() > 0;
    }
}
