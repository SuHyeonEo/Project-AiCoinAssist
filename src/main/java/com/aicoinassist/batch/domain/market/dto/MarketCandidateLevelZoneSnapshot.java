package com.aicoinassist.batch.domain.market.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelLabel;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelSourceType;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record MarketCandidateLevelZoneSnapshot(
        String symbol,
        String intervalValue,
        Instant snapshotTime,
        MarketCandidateLevelType zoneType,
        Integer zoneRank,
        BigDecimal currentPrice,
        BigDecimal representativePrice,
        BigDecimal zoneLow,
        BigDecimal zoneHigh,
        BigDecimal distanceFromCurrent,
        BigDecimal zoneStrengthScore,
        MarketCandidateLevelLabel strongestLevelLabel,
        MarketCandidateLevelSourceType strongestSourceType,
        Integer levelCount,
        List<MarketCandidateLevelLabel> includedLevelLabels,
        List<MarketCandidateLevelSourceType> includedSourceTypes,
        List<String> triggerFacts,
        String sourceDataVersion
) {
}
