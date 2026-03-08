package com.aicoinassist.batch.domain.report.dto;

import java.time.Instant;
import java.util.List;

public record AnalysisReportBatchRunResult(
        String runId,
        Instant startedAt,
        Instant finishedAt,
        long durationMillis,
        List<AnalysisReportBatchResult> assetResults
) {

    public int assetSuccessCount() {
        return (int) assetResults.stream()
                                 .filter(result -> !result.hasFailures())
                                 .count();
    }

    public int assetFailureCount() {
        return assetResults.size() - assetSuccessCount();
    }
}
