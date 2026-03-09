package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;

import java.util.List;

public record AnalysisContinuityContextPayload(
        AnalysisComparisonReference reference,
        String previousHeadline,
        List<String> carriedSignals,
        List<String> invalidatedSignals
) {
}
