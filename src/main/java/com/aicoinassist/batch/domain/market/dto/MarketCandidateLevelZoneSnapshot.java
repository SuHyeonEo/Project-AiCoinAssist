package com.aicoinassist.batch.domain.market.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelLabel;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelSourceType;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelType;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelZoneInteractionType;

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
        BigDecimal distanceToZone,
        BigDecimal zoneStrengthScore,
        MarketCandidateLevelZoneInteractionType interactionType,
        MarketCandidateLevelLabel strongestLevelLabel,
        MarketCandidateLevelSourceType strongestSourceType,
        Integer levelCount,
        Integer recentTestCount,
        Integer recentRejectionCount,
        Integer recentBreakCount,
        List<MarketCandidateLevelLabel> includedLevelLabels,
        List<MarketCandidateLevelSourceType> includedSourceTypes,
        List<String> triggerFacts,
        String sourceDataVersion
) {
}
