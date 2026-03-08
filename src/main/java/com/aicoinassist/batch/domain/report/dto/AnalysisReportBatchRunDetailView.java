package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionStatus;
import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionTriggerType;

import java.time.Instant;
import java.util.List;

public record AnalysisReportBatchRunDetailView(
        String runId,
        BatchExecutionStatus executionStatus,
        BatchExecutionTriggerType triggerType,
        String rerunSourceRunId,
        String engineVersion,
        Instant startedAt,
        Instant finishedAt,
        long durationMillis,
        int assetSuccessCount,
        int assetFailureCount,
        Instant storedTime,
        boolean rerunnable,
        List<String> rerunnableSymbols,
        List<AnalysisReportBatchAssetResultView> assetResults
) {
}
