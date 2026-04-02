package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisSummaryKeyMessagePayload(
        String primaryMessage,
        List<String> signalDetails,
        String continuityMessage
) {
}
