package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisDerivativeContextSummaryPayload(
        String currentStateSummary,
        String windowSummary,
        List<String> highlightDetails,
        List<String> riskSignals,
        Long nextFundingHours
) {
}
