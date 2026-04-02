package com.aicoinassist.batch.domain.market.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeDirection;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeSeverity;

public record MarketExternalRegimeSignalSnapshot(
        AnalysisExternalRegimeCategory category,
        String title,
        String detail,
        AnalysisExternalRegimeDirection direction,
        AnalysisExternalRegimeSeverity severity,
        String basisLabel
) {
}
