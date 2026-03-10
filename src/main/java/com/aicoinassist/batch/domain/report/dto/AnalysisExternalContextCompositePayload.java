package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeDirection;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeSeverity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AnalysisExternalContextCompositePayload(
        Instant snapshotTime,
        String sourceDataVersion,
        BigDecimal compositeRiskScore,
        AnalysisExternalRegimeDirection dominantDirection,
        AnalysisExternalRegimeSeverity highestSeverity,
        Integer supportiveSignalCount,
        Integer cautionarySignalCount,
        Integer headwindSignalCount,
        com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeCategory primarySignalCategory,
        String primarySignalTitle,
        String primarySignalDetail,
        List<AnalysisExternalRegimeSignal> regimeSignals,
        List<AnalysisExternalContextComparisonFact> comparisonFacts,
        List<AnalysisExternalContextHighlight> highlights
) {
}
