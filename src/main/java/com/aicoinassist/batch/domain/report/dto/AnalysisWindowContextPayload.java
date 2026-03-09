package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisWindowContextPayload(
        String summary,
        List<String> highlightDetails
) {
}
