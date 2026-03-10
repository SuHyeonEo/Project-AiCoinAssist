package com.aicoinassist.batch.domain.onchain.service;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.onchain.dto.OnchainWindowSummarySnapshot;
import com.aicoinassist.batch.domain.onchain.entity.OnchainFactSnapshotEntity;
import com.aicoinassist.batch.domain.onchain.repository.OnchainFactSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnchainWindowSummarySnapshotService {

    private static final int RATIO_SCALE = 8;

    private final OnchainFactSnapshotRepository onchainFactSnapshotRepository;

    public OnchainWindowSummarySnapshot create(
            OnchainFactSnapshotEntity currentSnapshot,
            MarketWindowType windowType
    ) {
        Instant windowEndTime = currentSnapshot.getSnapshotTime();
        Instant windowStartTime = windowEndTime.minus(windowType.days(), ChronoUnit.DAYS);

        List<OnchainFactSnapshotEntity> snapshots = onchainFactSnapshotRepository
                .findAllBySymbolAndSnapshotTimeGreaterThanEqualAndSnapshotTimeLessThanEqualOrderBySnapshotTimeAscIdAsc(
                        currentSnapshot.getSymbol(),
                        windowStartTime,
                        windowEndTime
                );

        if (snapshots.isEmpty()) {
            throw new IllegalStateException(
                    "No onchain snapshots found for window summary: symbol=%s window=%s end=%s"
                            .formatted(currentSnapshot.getSymbol(), windowType.name(), currentSnapshot.getSnapshotTime())
            );
        }

        BigDecimal averageActiveAddressCount = average(snapshots.stream().map(OnchainFactSnapshotEntity::getActiveAddressCount).toList());
        BigDecimal averageTransactionCount = average(snapshots.stream().map(OnchainFactSnapshotEntity::getTransactionCount).toList());
        BigDecimal averageMarketCapUsd = average(snapshots.stream().map(OnchainFactSnapshotEntity::getMarketCapUsd).toList());

        return new OnchainWindowSummarySnapshot(
                currentSnapshot.getSymbol(),
                currentSnapshot.getAssetCode(),
                windowType,
                windowStartTime,
                windowEndTime,
                snapshots.size(),
                currentSnapshot.getActiveAddressCount(),
                averageActiveAddressCount,
                deltaRatio(currentSnapshot.getActiveAddressCount(), averageActiveAddressCount),
                currentSnapshot.getTransactionCount(),
                averageTransactionCount,
                deltaRatio(currentSnapshot.getTransactionCount(), averageTransactionCount),
                currentSnapshot.getMarketCapUsd(),
                averageMarketCapUsd,
                deltaRatio(currentSnapshot.getMarketCapUsd(), averageMarketCapUsd),
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
            OnchainFactSnapshotEntity currentSnapshot,
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
