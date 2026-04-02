package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.Candle;
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
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MarketWindowSummarySnapshotService {

    private static final int RATIO_SCALE = 8;

    private final MarketIndicatorSnapshotRepository marketIndicatorSnapshotRepository;
    private final MarketCandleRawRepository marketCandleRawRepository;

    public MarketWindowSummarySnapshot create(
            MarketIndicatorSnapshotEntity currentSnapshot,
            MarketWindowType windowType,
            List<Candle> candles
    ) {
        Instant windowEndTime = currentSnapshot.getSnapshotTime();
        Instant windowStartTime = windowEndTime.minus(windowType.days(), ChronoUnit.DAYS);
        List<Candle> windowCandles = candles.stream()
                .filter(candle -> !candle.openTime().isBefore(windowStartTime))
                .filter(candle -> !candle.openTime().isAfter(windowEndTime))
                .toList();

        if (windowCandles.isEmpty()) {
            throw new IllegalStateException(
                    "No raw candles found for window summary: symbol=%s interval=%s window=%s end=%s"
                            .formatted(
                                    currentSnapshot.getSymbol(),
                                    currentSnapshot.getIntervalValue(),
                                    windowType.name(),
                                    currentSnapshot.getSnapshotTime()
                            )
            );
        }

        return createFromCandles(currentSnapshot, windowType, windowStartTime, windowEndTime, windowCandles);
    }

    private MarketWindowSummarySnapshot createFromCandles(
            MarketIndicatorSnapshotEntity currentSnapshot,
            MarketWindowType windowType,
            Instant windowStartTime,
            Instant windowEndTime,
            List<Candle> candles
    ) {
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

        List<MarketCandleRawEntity> rawCandles = marketCandleRawRepository
                .findAllBySymbolAndIntervalValueAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualAndValidationStatusOrderByOpenTimeAsc(
                        currentSnapshot.getSymbol(),
                        currentSnapshot.getIntervalValue(),
                        windowStartTime,
                        windowEndTime,
                        RawDataValidationStatus.VALID
                );

        if (rawCandles.isEmpty()) {
            throw new IllegalStateException(
                    "No valid raw candles found for participation summary: symbol=%s interval=%s window=%s end=%s"
                            .formatted(
                                    currentSnapshot.getSymbol(),
                                    currentSnapshot.getIntervalValue(),
                                    windowType.name(),
                                    currentSnapshot.getSnapshotTime()
                            )
            );
        }

        BigDecimal windowHigh = candles.stream()
                                       .map(Candle::high)
                                       .max(Comparator.naturalOrder())
                                       .orElseThrow();
        BigDecimal windowLow = candles.stream()
                                      .map(Candle::low)
                                      .min(Comparator.naturalOrder())
                                      .orElseThrow();
        MarketCandleRawEntity currentRawCandle = rawCandles.get(rawCandles.size() - 1);
        BigDecimal windowRange = windowHigh.subtract(windowLow);
        BigDecimal averageVolume = average(candles.stream().map(Candle::volume).toList());
        BigDecimal averageQuoteAssetVolume = average(rawCandles.stream().map(MarketCandleRawEntity::getQuoteAssetVolume).toList());
        BigDecimal averageTradeCount = average(
                rawCandles.stream()
                          .map(MarketCandleRawEntity::getNumberOfTrades)
                          .filter(Objects::nonNull)
                          .map(BigDecimal::valueOf)
                          .toList()
        );
        BigDecimal averageAtr = average(indicatorSnapshots.stream().map(MarketIndicatorSnapshotEntity::getAtr14).toList());
        BigDecimal currentVolume = candles.get(candles.size() - 1).volume();
        BigDecimal currentQuoteAssetVolume = currentRawCandle.getQuoteAssetVolume();
        BigDecimal currentTradeCount = currentRawCandle.getNumberOfTrades() == null
                ? null
                : BigDecimal.valueOf(currentRawCandle.getNumberOfTrades());
        BigDecimal currentAtr = currentSnapshot.getAtr14();
        BigDecimal currentTakerBuyQuoteRatio = ratio(
                currentRawCandle.getTakerBuyQuoteAssetVolume(),
                currentRawCandle.getQuoteAssetVolume()
        );

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
                averageQuoteAssetVolume,
                averageTradeCount,
                averageAtr,
                currentVolume,
                currentQuoteAssetVolume,
                currentTradeCount,
                currentAtr,
                deltaRatio(currentVolume, averageVolume),
                deltaRatio(currentQuoteAssetVolume, averageQuoteAssetVolume),
                deltaRatio(currentTradeCount, averageTradeCount),
                currentTakerBuyQuoteRatio,
                deltaRatio(currentAtr, averageAtr),
                buildSourceDataVersion(currentSnapshot, windowType, windowStartTime, windowEndTime, rawCandles.size())
        );
    }

    private BigDecimal average(List<BigDecimal> values) {
        List<BigDecimal> nonNullValues = values.stream()
                                               .filter(Objects::nonNull)
                                               .toList();
        if (nonNullValues.isEmpty()) {
            return null;
        }

        BigDecimal sum = nonNullValues.stream()
                               .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(nonNullValues.size()), RATIO_SCALE, RoundingMode.HALF_UP);
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
