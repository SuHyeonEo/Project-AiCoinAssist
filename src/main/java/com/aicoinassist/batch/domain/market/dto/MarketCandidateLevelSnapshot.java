package com.aicoinassist.batch.domain.market.dto;

import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelLabel;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelSourceType;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record MarketCandidateLevelSnapshot(
        String symbol,
        String intervalValue,
        Instant snapshotTime,
        Instant referenceTime,
        MarketCandidateLevelType levelType,
        MarketCandidateLevelLabel levelLabel,
        MarketCandidateLevelSourceType sourceType,
        BigDecimal currentPrice,
        BigDecimal levelPrice,
        BigDecimal distanceFromCurrent,
        BigDecimal strengthScore,
        Integer reactionCount,
        Integer clusterSize,
        String rationale,
        List<String> triggerFacts,
        String sourceDataVersion
) {
}
