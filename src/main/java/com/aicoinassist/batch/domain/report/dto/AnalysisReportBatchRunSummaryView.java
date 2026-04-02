package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionStatus;
import com.aicoinassist.batch.domain.report.enumtype.BatchExecutionTriggerType;

import java.time.Instant;

public record AnalysisReportBatchRunSummaryView(
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
        boolean rerunnable
) {
}
