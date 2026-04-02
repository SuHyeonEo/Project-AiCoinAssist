package com.aicoinassist.batch.domain.report.dto;

public record AnalysisWindowContextSummaryPayload(
        String rangeSummary,
        String rangePositionSummary,
        String volatilitySummary
) {
}
