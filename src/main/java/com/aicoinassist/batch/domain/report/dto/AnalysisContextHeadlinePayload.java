package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineImportance;

public record AnalysisContextHeadlinePayload(
        AnalysisContextHeadlineCategory category,
        String title,
        String detail,
        AnalysisContextHeadlineImportance importance
) {
}
