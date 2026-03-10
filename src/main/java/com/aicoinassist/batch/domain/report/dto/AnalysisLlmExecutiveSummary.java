package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisConfidenceLevel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisOutlookType;

public record AnalysisLlmExecutiveSummary(
        String headline,
        AnalysisOutlookType outlook,
        AnalysisConfidenceLevel confidence,
        String primaryMessage
) {
}
