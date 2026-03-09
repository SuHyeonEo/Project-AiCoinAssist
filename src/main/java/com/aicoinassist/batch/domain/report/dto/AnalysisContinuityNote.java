package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;

import java.time.Instant;

public record AnalysisContinuityNote(
        AnalysisComparisonReference reference,
        Instant previousAnalysisBasisTime,
        String summary
) {
}
