package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketLevelContextSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelZoneSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelType;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelZoneInteractionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class MarketLevelContextSnapshotService {

    private static final int SCALE = 8;

    public MarketLevelContextSnapshot create(
            MarketIndicatorSnapshotEntity snapshot,
            List<MarketCandidateLevelZoneSnapshotEntity> zoneEntities
    ) {
        MarketCandidateLevelZoneSnapshotEntity supportZone = nearestZone(zoneEntities, MarketCandidateLevelType.SUPPORT);
        MarketCandidateLevelZoneSnapshotEntity resistanceZone = nearestZone(zoneEntities, MarketCandidateLevelType.RESISTANCE);

        return new MarketLevelContextSnapshot(
                snapshot.getSymbol(),
                snapshot.getIntervalValue(),
                snapshot.getSnapshotTime(),
                snapshot.getCurrentPrice(),
                supportZone == null ? null : supportZone.getZoneRank(),
                supportZone == null ? null : supportZone.getRepresentativePrice(),
                supportZone == null ? null : supportZone.getZoneLow(),
                supportZone == null ? null : supportZone.getZoneHigh(),
                supportZone == null ? null : supportZone.getDistanceToZone(),
                supportZone == null ? null : supportZone.getZoneStrengthScore(),
                supportZone == null ? null : MarketCandidateLevelZoneInteractionType.valueOf(supportZone.getInteractionType()),
                supportZone == null ? null : supportZone.getRecentTestCount(),
                supportZone == null ? null : supportZone.getRecentRejectionCount(),
                supportZone == null ? null : supportZone.getRecentBreakCount(),
                breakRisk(supportZone, MarketCandidateLevelType.SUPPORT),
                resistanceZone == null ? null : resistanceZone.getZoneRank(),
                resistanceZone == null ? null : resistanceZone.getRepresentativePrice(),
                resistanceZone == null ? null : resistanceZone.getZoneLow(),
                resistanceZone == null ? null : resistanceZone.getZoneHigh(),
                resistanceZone == null ? null : resistanceZone.getDistanceToZone(),
                resistanceZone == null ? null : resistanceZone.getZoneStrengthScore(),
                resistanceZone == null ? null : MarketCandidateLevelZoneInteractionType.valueOf(resistanceZone.getInteractionType()),
                resistanceZone == null ? null : resistanceZone.getRecentTestCount(),
                resistanceZone == null ? null : resistanceZone.getRecentRejectionCount(),
                resistanceZone == null ? null : resistanceZone.getRecentBreakCount(),
                breakRisk(resistanceZone, MarketCandidateLevelType.RESISTANCE),
                sourceDataVersion(snapshot, supportZone, resistanceZone)
        );
    }

    private MarketCandidateLevelZoneSnapshotEntity nearestZone(
            List<MarketCandidateLevelZoneSnapshotEntity> zoneEntities,
            MarketCandidateLevelType zoneType
    ) {
        return zoneEntities.stream()
                           .filter(entity -> entity.getZoneType().equals(zoneType.name()))
                           .min(Comparator.comparing(MarketCandidateLevelZoneSnapshotEntity::getZoneRank))
                           .orElse(null);
    }

    private BigDecimal breakRisk(
            MarketCandidateLevelZoneSnapshotEntity zone,
            MarketCandidateLevelType zoneType
    ) {
        if (zone == null) {
            return null;
        }

        BigDecimal tests = BigDecimal.valueOf(Math.max(zone.getRecentTestCount(), 1));
        BigDecimal baseRisk = BigDecimal.valueOf(zone.getRecentBreakCount())
                                        .divide(tests, SCALE, RoundingMode.HALF_UP);
        BigDecimal interactionAdjustment = switch (MarketCandidateLevelZoneInteractionType.valueOf(zone.getInteractionType())) {
            case ABOVE_ZONE -> zoneType == MarketCandidateLevelType.SUPPORT
                    ? new BigDecimal("0.05")
                    : new BigDecimal("0.40");
            case INSIDE_ZONE -> new BigDecimal("0.20");
            case BELOW_ZONE -> zoneType == MarketCandidateLevelType.SUPPORT
                    ? new BigDecimal("0.40")
                    : new BigDecimal("0.05");
        };
        BigDecimal rejectionOffset = BigDecimal.valueOf(Math.min(zone.getRecentRejectionCount(), 4))
                                               .multiply(new BigDecimal("0.04"));
        BigDecimal risk = baseRisk.add(interactionAdjustment).subtract(rejectionOffset);
        if (risk.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        return risk.min(BigDecimal.ONE).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private String sourceDataVersion(
            MarketIndicatorSnapshotEntity snapshot,
            MarketCandidateLevelZoneSnapshotEntity supportZone,
            MarketCandidateLevelZoneSnapshotEntity resistanceZone
    ) {
        return "indicator=" + snapshot.getSourceDataVersion()
                + ";supportZone=" + (supportZone == null ? "NONE" : supportZone.getSourceDataVersion())
                + ";resistanceZone=" + (resistanceZone == null ? "NONE" : resistanceZone.getSourceDataVersion());
    }
}
