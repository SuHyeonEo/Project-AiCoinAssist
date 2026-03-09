package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketContextWindowSummarySnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.market.repository.MarketContextSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketContextWindowSummarySnapshotService {

    private static final int RATIO_SCALE = 8;

    private final MarketContextSnapshotRepository marketContextSnapshotRepository;

    public MarketContextWindowSummarySnapshot create(
            MarketContextSnapshotEntity currentSnapshot,
            MarketWindowType windowType
    ) {
        Instant windowEndTime = currentSnapshot.getSnapshotTime();
        Instant windowStartTime = windowEndTime.minus(windowType.days(), ChronoUnit.DAYS);

        List<MarketContextSnapshotEntity> snapshots = marketContextSnapshotRepository
                .findAllBySymbolAndSnapshotTimeGreaterThanEqualAndSnapshotTimeLessThanEqualOrderBySnapshotTimeAscIdAsc(
                        currentSnapshot.getSymbol(),
                        windowStartTime,
                        windowEndTime
                );

        if (snapshots.isEmpty()) {
            throw new IllegalStateException(
                    "No market context snapshots found for context window summary: symbol=%s window=%s end=%s"
                            .formatted(
                                    currentSnapshot.getSymbol(),
                                    windowType.name(),
                                    currentSnapshot.getSnapshotTime()
                            )
            );
        }

        BigDecimal averageOpenInterest = average(snapshots.stream().map(MarketContextSnapshotEntity::getOpenInterest).toList());
        BigDecimal averageFundingRate = average(snapshots.stream().map(MarketContextSnapshotEntity::getLastFundingRate).toList());
        BigDecimal averageBasisRate = average(snapshots.stream().map(MarketContextSnapshotEntity::getMarkIndexBasisRate).toList());

        return new MarketContextWindowSummarySnapshot(
                currentSnapshot.getSymbol(),
                windowType,
                windowStartTime,
                windowEndTime,
                snapshots.size(),
                currentSnapshot.getOpenInterest(),
                averageOpenInterest,
                deltaRatio(currentSnapshot.getOpenInterest(), averageOpenInterest),
                currentSnapshot.getLastFundingRate(),
                averageFundingRate,
                deltaRatio(currentSnapshot.getLastFundingRate(), averageFundingRate),
                currentSnapshot.getMarkIndexBasisRate(),
                averageBasisRate,
                deltaRatio(currentSnapshot.getMarkIndexBasisRate(), averageBasisRate),
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

    private String buildSourceDataVersion(
            MarketContextSnapshotEntity currentSnapshot,
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
