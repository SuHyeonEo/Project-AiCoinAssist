package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketExternalContextWindowSummarySnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketExternalContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.market.repository.MarketExternalContextSnapshotRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class MarketExternalContextWindowSummarySnapshotService {

    private static final int RATIO_SCALE = 8;

    private final MarketExternalContextSnapshotRepository marketExternalContextSnapshotRepository;

    public MarketExternalContextWindowSummarySnapshotService(
            MarketExternalContextSnapshotRepository marketExternalContextSnapshotRepository
    ) {
        this.marketExternalContextSnapshotRepository = marketExternalContextSnapshotRepository;
    }

    public MarketExternalContextWindowSummarySnapshot create(
            MarketExternalContextSnapshotEntity currentSnapshot,
            MarketWindowType windowType
    ) {
        Instant windowEndTime = currentSnapshot.getSnapshotTime();
        Instant windowStartTime = windowEndTime.minus(windowType.days(), ChronoUnit.DAYS);

        List<MarketExternalContextSnapshotEntity> snapshots = marketExternalContextSnapshotRepository
                .findAllBySymbolAndSnapshotTimeGreaterThanEqualAndSnapshotTimeLessThanEqualOrderBySnapshotTimeAscIdAsc(
                        currentSnapshot.getSymbol(),
                        windowStartTime,
                        windowEndTime
                );

        if (snapshots.isEmpty()) {
            throw new IllegalStateException(
                    "No external context snapshots found for window summary: symbol=%s window=%s end=%s"
                            .formatted(currentSnapshot.getSymbol(), windowType.name(), currentSnapshot.getSnapshotTime())
            );
        }

        BigDecimal averageCompositeRiskScore = average(
                snapshots.stream().map(MarketExternalContextSnapshotEntity::getCompositeRiskScore).toList()
        );

        return new MarketExternalContextWindowSummarySnapshot(
                currentSnapshot.getSymbol(),
                windowType,
                windowStartTime,
                windowEndTime,
                snapshots.size(),
                currentSnapshot.getCompositeRiskScore(),
                averageCompositeRiskScore,
                deltaRatio(currentSnapshot.getCompositeRiskScore(), averageCompositeRiskScore),
                supportiveDominanceCount(snapshots),
                cautionaryDominanceCount(snapshots),
                headwindDominanceCount(snapshots),
                highSeverityCount(snapshots),
                buildSourceDataVersion(currentSnapshot, windowType, windowStartTime, windowEndTime, snapshots.size())
        );
    }

    private BigDecimal average(List<BigDecimal> values) {
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), RATIO_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal deltaRatio(BigDecimal currentValue, BigDecimal averageValue) {
        if (averageValue == null || averageValue.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return currentValue.subtract(averageValue)
                           .divide(averageValue, RATIO_SCALE, RoundingMode.HALF_UP);
    }

    private int supportiveDominanceCount(List<MarketExternalContextSnapshotEntity> snapshots) {
        return (int) snapshots.stream()
                              .filter(snapshot -> "SUPPORTIVE".equals(snapshot.getDominantDirection()))
                              .count();
    }

    private int cautionaryDominanceCount(List<MarketExternalContextSnapshotEntity> snapshots) {
        return (int) snapshots.stream()
                              .filter(snapshot -> "CAUTIONARY".equals(snapshot.getDominantDirection()))
                              .count();
    }

    private int headwindDominanceCount(List<MarketExternalContextSnapshotEntity> snapshots) {
        return (int) snapshots.stream()
                              .filter(snapshot -> "HEADWIND".equals(snapshot.getDominantDirection()))
                              .count();
    }

    private int highSeverityCount(List<MarketExternalContextSnapshotEntity> snapshots) {
        return (int) snapshots.stream()
                              .filter(snapshot -> "HIGH".equals(snapshot.getHighestSeverity()))
                              .count();
    }

    private String buildSourceDataVersion(
            MarketExternalContextSnapshotEntity currentSnapshot,
            MarketWindowType windowType,
            Instant windowStartTime,
            Instant windowEndTime,
            int sampleCount
    ) {
        return currentSnapshot.getSourceDataVersion()
                + ";windowType=" + windowType.name()
                + ";windowStartTime=" + windowStartTime
                + ";windowEndTime=" + windowEndTime
                + ";sampleCount=" + sampleCount;
    }
}
