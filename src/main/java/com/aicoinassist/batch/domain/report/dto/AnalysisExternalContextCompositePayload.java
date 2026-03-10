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
        List<AnalysisExternalContextHighlight> highlights,
        List<AnalysisExternalContextWindowSummary> windowSummaries,
        List<AnalysisExternalRegimeTransition> transitions,
        AnalysisExternalRegimePersistence persistence,
        AnalysisExternalRegimeStatePayload state
) {
    public AnalysisExternalContextCompositePayload(
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
        this(
                snapshotTime,
                sourceDataVersion,
                compositeRiskScore,
                dominantDirection,
                highestSeverity,
                supportiveSignalCount,
                cautionarySignalCount,
                headwindSignalCount,
                primarySignalCategory,
                primarySignalTitle,
                primarySignalDetail,
                regimeSignals,
                comparisonFacts,
                highlights,
                List.of(),
                List.of(),
                null,
                null
        );
    }
}
