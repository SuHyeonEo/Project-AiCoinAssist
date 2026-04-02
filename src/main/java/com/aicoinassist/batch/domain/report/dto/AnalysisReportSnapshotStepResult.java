package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;

public record AnalysisReportSnapshotStepResult(
        CandleInterval interval,
        boolean success,
        String errorMessage
) {
}
