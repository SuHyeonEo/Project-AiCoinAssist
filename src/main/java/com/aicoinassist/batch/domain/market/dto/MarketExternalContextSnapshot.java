package com.aicoinassist.batch.domain.market.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeDirection;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeSeverity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record MarketExternalContextSnapshot(
        String symbol,
        Instant snapshotTime,
        Instant derivativeSnapshotTime,
        Instant macroSnapshotTime,
        Instant sentimentSnapshotTime,
        Instant onchainSnapshotTime,
        String sourceDataVersion,
        BigDecimal compositeRiskScore,
        AnalysisExternalRegimeDirection dominantDirection,
        AnalysisExternalRegimeSeverity highestSeverity,
        Integer supportiveSignalCount,
        Integer cautionarySignalCount,
        Integer headwindSignalCount,
        AnalysisExternalRegimeCategory primarySignalCategory,
        String primarySignalTitle,
        String primarySignalDetail,
        List<MarketExternalRegimeSignalSnapshot> regimeSignals
) {
}
