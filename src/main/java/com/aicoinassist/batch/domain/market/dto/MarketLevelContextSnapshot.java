package com.aicoinassist.batch.domain.market.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelZoneInteractionType;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketLevelContextSnapshot(
        String symbol,
        String intervalValue,
        Instant snapshotTime,
        BigDecimal currentPrice,
        Integer supportZoneRank,
        BigDecimal supportRepresentativePrice,
        BigDecimal supportZoneLow,
        BigDecimal supportZoneHigh,
        BigDecimal supportDistanceToZone,
        BigDecimal supportZoneStrength,
        MarketCandidateLevelZoneInteractionType supportInteractionType,
        Integer supportRecentTestCount,
        Integer supportRecentRejectionCount,
        Integer supportRecentBreakCount,
        BigDecimal supportBreakRisk,
        Integer resistanceZoneRank,
        BigDecimal resistanceRepresentativePrice,
        BigDecimal resistanceZoneLow,
        BigDecimal resistanceZoneHigh,
        BigDecimal resistanceDistanceToZone,
        BigDecimal resistanceZoneStrength,
        MarketCandidateLevelZoneInteractionType resistanceInteractionType,
        Integer resistanceRecentTestCount,
        Integer resistanceRecentRejectionCount,
        Integer resistanceRecentBreakCount,
        BigDecimal resistanceBreakRisk,
        String sourceDataVersion
) {
}
