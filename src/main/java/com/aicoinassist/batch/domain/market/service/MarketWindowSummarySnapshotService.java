package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketWindowSummarySnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.repository.MarketCandleRawRepository;
import com.aicoinassist.batch.domain.market.repository.MarketIndicatorSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketWindowSummarySnapshotService {

    private static final String BINANCE_SOURCE = "BINANCE";
    private static final int RATIO_SCALE = 8;

    private final MarketCandleRawRepository marketCandleRawRepository;
    private final MarketIndicatorSnapshotRepository marketIndicatorSnapshotRepository;

    public MarketWindowSummarySnapshot create(
            MarketIndicatorSnapshotEntity currentSnapshot,
            MarketWindowType windowType
    ) {
        Instant windowEndTime = currentSnapshot.getSnapshotTime();
        Instant windowStartTime = windowEndTime.minus(windowType.days(), ChronoUnit.DAYS);

        List<MarketCandleRawEntity> candles = marketCandleRawRepository
                .findAllBySourceAndSymbolAndIntervalValueAndValidationStatusAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualOrderByOpenTimeAsc(
                        BINANCE_SOURCE,
                        currentSnapshot.getSymbol(),
                        currentSnapshot.getIntervalValue(),
                        RawDataValidationStatus.VALID,
                        windowStartTime,
                        windowEndTime
                );

        if (candles.isEmpty()) {
            throw new IllegalStateException(
                    "No valid candle raw rows found for window summary: symbol=%s interval=%s window=%s end=%s"
                            .formatted(
                                    currentSnapshot.getSymbol(),
                                    currentSnapshot.getIntervalValue(),
                                    windowType.name(),
                                    currentSnapshot.getSnapshotTime()
                            )
            );
        }

        List<MarketIndicatorSnapshotEntity> indicatorSnapshots = marketIndicatorSnapshotRepository
                .findAllBySymbolAndIntervalValueAndSnapshotTimeGreaterThanEqualAndSnapshotTimeLessThanEqualOrderBySnapshotTimeAsc(
                        currentSnapshot.getSymbol(),
                        currentSnapshot.getIntervalValue(),
                        windowStartTime,
                        windowEndTime
                );

        if (indicatorSnapshots.isEmpty()) {
            throw new IllegalStateException(
                    "No market indicator snapshots found for window summary: symbol=%s interval=%s window=%s end=%s"
                            .formatted(
                                    currentSnapshot.getSymbol(),
                                    currentSnapshot.getIntervalValue(),
                                    windowType.name(),
                                    currentSnapshot.getSnapshotTime()
                            )
            );
        }

        BigDecimal windowHigh = candles.stream()
                                       .map(MarketCandleRawEntity::getHighPrice)
                                       .max(Comparator.naturalOrder())
                                       .orElseThrow();
        BigDecimal windowLow = candles.stream()
                                      .map(MarketCandleRawEntity::getLowPrice)
                                      .min(Comparator.naturalOrder())
                                      .orElseThrow();
        BigDecimal windowRange = windowHigh.subtract(windowLow);
        BigDecimal averageVolume = average(candles.stream().map(MarketCandleRawEntity::getVolume).toList());
        BigDecimal averageAtr = average(indicatorSnapshots.stream().map(MarketIndicatorSnapshotEntity::getAtr14).toList());
        BigDecimal currentVolume = candles.get(candles.size() - 1).getVolume();
        BigDecimal currentAtr = currentSnapshot.getAtr14();

        return new MarketWindowSummarySnapshot(
                currentSnapshot.getSymbol(),
                currentSnapshot.getIntervalValue(),
                windowType,
                windowStartTime,
                windowEndTime,
                candles.size(),
                currentSnapshot.getCurrentPrice(),
                windowHigh,
                windowLow,
                windowRange,
                ratio(currentSnapshot.getCurrentPrice().subtract(windowLow), windowRange),
                ratio(windowHigh.subtract(currentSnapshot.getCurrentPrice()), windowHigh),
                ratio(currentSnapshot.getCurrentPrice().subtract(windowLow), windowLow),
                averageVolume,
                averageAtr,
                currentVolume,
                currentAtr,
                deltaRatio(currentVolume, averageVolume),
                deltaRatio(currentAtr, averageAtr),
                buildSourceDataVersion(currentSnapshot, windowType, windowStartTime, windowEndTime, candles.size())
        );
    }

    private BigDecimal average(List<BigDecimal> values) {
        BigDecimal sum = values.stream()
                               .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), RATIO_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return numerator.divide(denominator, RATIO_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal deltaRatio(BigDecimal currentValue, BigDecimal averageValue) {
        if (averageValue == null || averageValue.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return currentValue.subtract(averageValue)
                           .divide(averageValue, RATIO_SCALE, RoundingMode.HALF_UP);
    }

    private String buildSourceDataVersion(
            MarketIndicatorSnapshotEntity currentSnapshot,
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
