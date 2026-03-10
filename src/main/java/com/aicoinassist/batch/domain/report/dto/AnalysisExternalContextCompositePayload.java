package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeDirection;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeSeverity;

import java.math.BigDecimal;
import java.util.List;

public record AnalysisExternalContextCompositePayload(
        BigDecimal compositeRiskScore,
        AnalysisExternalRegimeDirection dominantDirection,
        AnalysisExternalRegimeSeverity highestSeverity,
        Integer supportiveSignalCount,
        Integer cautionarySignalCount,
        Integer headwindSignalCount,
        String primarySignalTitle,
        String primarySignalDetail,
        List<AnalysisExternalRegimeSignal> regimeSignals
) {
}
