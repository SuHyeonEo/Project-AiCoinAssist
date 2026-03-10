package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeDirection;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeSeverity;

public record AnalysisExternalRegimeSignal(
        AnalysisExternalRegimeCategory category,
        String title,
        String detail,
        AnalysisExternalRegimeDirection direction,
        AnalysisExternalRegimeSeverity severity,
        String basisLabel
) {
}
