package com.aicoinassist.batch.domain.macro.service;

import com.aicoinassist.batch.domain.macro.dto.MacroContextWindowSummarySnapshot;
import com.aicoinassist.batch.domain.macro.entity.MacroContextSnapshotEntity;
import com.aicoinassist.batch.domain.macro.repository.MacroContextSnapshotRepository;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MacroContextWindowSummarySnapshotService {

    private static final int RATIO_SCALE = 8;

    private final MacroContextSnapshotRepository macroContextSnapshotRepository;

    public MacroContextWindowSummarySnapshot create(
            MacroContextSnapshotEntity currentSnapshot,
            MarketWindowType windowType
    ) {
        Instant windowEndTime = currentSnapshot.getSnapshotTime();
        Instant windowStartTime = windowEndTime.minus(windowType.days(), ChronoUnit.DAYS);

        List<MacroContextSnapshotEntity> snapshots = macroContextSnapshotRepository
                .findAllBySnapshotTimeGreaterThanEqualAndSnapshotTimeLessThanEqualOrderBySnapshotTimeAscIdAsc(
                        windowStartTime,
                        windowEndTime
                );

        if (snapshots.isEmpty()) {
            throw new IllegalStateException(
                    "No macro context snapshots found for window summary: window=%s end=%s"
                            .formatted(windowType.name(), currentSnapshot.getSnapshotTime())
            );
        }

        BigDecimal averageDxy = average(snapshots.stream().map(MacroContextSnapshotEntity::getDxyProxyValue).toList());
        BigDecimal averageUs10y = average(snapshots.stream().map(MacroContextSnapshotEntity::getUs10yYieldValue).toList());
        BigDecimal averageUsdKrw = average(snapshots.stream().map(MacroContextSnapshotEntity::getUsdKrwValue).toList());

        return new MacroContextWindowSummarySnapshot(
                windowType,
                windowStartTime,
                windowEndTime,
                snapshots.size(),
                currentSnapshot.getDxyProxyValue(),
                averageDxy,
                deltaRatio(currentSnapshot.getDxyProxyValue(), averageDxy),
                currentSnapshot.getUs10yYieldValue(),
                averageUs10y,
                deltaRatio(currentSnapshot.getUs10yYieldValue(), averageUs10y),
                currentSnapshot.getUsdKrwValue(),
                averageUsdKrw,
                deltaRatio(currentSnapshot.getUsdKrwValue(), averageUsdKrw),
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
            MacroContextSnapshotEntity currentSnapshot,
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
