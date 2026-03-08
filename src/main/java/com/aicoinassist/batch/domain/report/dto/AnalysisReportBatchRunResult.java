package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionStatus;

import java.time.Instant;
import java.util.List;

public record AnalysisReportBatchRunResult(
        String runId,
        Instant startedAt,
        Instant finishedAt,
        long durationMillis,
        List<AnalysisReportBatchResult> assetResults
) {

    public AnalysisReportBatchRunResult {
        assetResults = List.copyOf(assetResults);
    }

    public int assetSuccessCount() {
        return (int) assetResults.stream()
                                 .filter(result -> result.status() == BatchExecutionStatus.SUCCESS)
                                 .count();
    }

    public int assetFailureCount() {
        return assetResults.size() - assetSuccessCount();
    }

    public BatchExecutionStatus status() {
        if (assetResults.isEmpty()) {
            return BatchExecutionStatus.SUCCESS;
        }
        if (assetResults.stream().allMatch(result -> result.status() == BatchExecutionStatus.SUCCESS)) {
            return BatchExecutionStatus.SUCCESS;
        }
        if (assetResults.stream().allMatch(result -> result.status() == BatchExecutionStatus.FAILED)) {
            return BatchExecutionStatus.FAILED;
        }
        return BatchExecutionStatus.PARTIAL_FAILURE;
    }
}
