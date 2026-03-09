package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisConfidenceLevel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisOutlookType;

public record AnalysisSummaryPayload(
        String headline,
        AnalysisOutlookType outlook,
        AnalysisConfidenceLevel confidence,
        AnalysisSummaryKeyMessagePayload keyMessage,
        java.util.List<AnalysisContextHeadlinePayload> signalHeadlines
) {
}
