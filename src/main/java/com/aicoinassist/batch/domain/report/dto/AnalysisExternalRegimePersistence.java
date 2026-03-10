package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeDirection;

import java.math.BigDecimal;

public record AnalysisExternalRegimePersistence(
        MarketWindowType windowType,
        AnalysisExternalRegimeDirection dominantDirection,
        BigDecimal dominantDirectionShare,
        BigDecimal highSeverityShare,
        BigDecimal persistenceScore,
        String summary
) {
}
