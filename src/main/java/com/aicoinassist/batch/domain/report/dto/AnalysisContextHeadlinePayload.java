package com.aicoinassist.batch.domain.report.dto;

public record AnalysisContextHeadlinePayload(
        String category,
        String title,
        String detail,
        String importance
) {
}
