package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisWindowContextPayload(
        AnalysisContextHeadlinePayload headline,
        String summary,
        List<String> highlightDetails
) {
}
