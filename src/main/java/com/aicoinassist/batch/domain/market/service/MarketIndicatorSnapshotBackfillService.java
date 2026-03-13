package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.indicator.calculator.AtrCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.BollingerBandsCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.MacdCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.MovingAverageCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.RsiCalculator;
import com.aicoinassist.batch.domain.market.dto.Candle;
import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.repository.MarketCandleRawRepository;
import com.aicoinassist.batch.domain.market.repository.MarketIndicatorSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketIndicatorSnapshotBackfillService {

    private static final int INDICATOR_LOOKBACK_CANDLES = 120;
    private static final int SNAPSHOT_REBUILD_OVERLAP = 3;

    private final MarketCandleRawRepository marketCandleRawRepository;
    private final MarketIndicatorSnapshotRepository marketIndicatorSnapshotRepository;
    private final MovingAverageCalculator movingAverageCalculator;
    private final RsiCalculator rsiCalculator;
    private final MacdCalculator macdCalculator;
    private final AtrCalculator atrCalculator;
    private final BollingerBandsCalculator bollingerBandsCalculator;
    private final Clock clock;

    @Transactional
    public int rebuildFromRaw(String symbol, CandleInterval interval) {
        Instant now = clock.instant();
        Instant latestClosedOpenTime = interval.latestClosedOpenTime(now);
        if (latestClosedOpenTime == null) {
            return 0;
        }

        Instant historyWindowStart = latestClosedOpenTime.minus(
                interval.duration().multipliedBy(interval.defaultBackfillLimit() - 1L)
        );
        MarketIndicatorSnapshotEntity latestSnapshot = marketIndicatorSnapshotRepository
                .findTopBySymbolAndIntervalValueOrderBySnapshotTimeDescIdDesc(symbol, interval.value())
                .orElse(null);
        Instant queryStartOpenTime = determineQueryStartOpenTime(historyWindowStart, latestSnapshot, interval);
        Instant rebuildFromSnapshotTime = determineRebuildFromSnapshotTime(latestSnapshot, interval);

        List<MarketCandleRawEntity> rawCandles = marketCandleRawRepository
                .findAllBySymbolAndIntervalValueAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualAndValidationStatusOrderByOpenTimeAsc(
                        symbol,
                        interval.value(),
                        queryStartOpenTime,
                        latestClosedOpenTime,
                        RawDataValidationStatus.VALID
                );
        if (rawCandles.size() < INDICATOR_LOOKBACK_CANDLES) {
            return 0;
        }

        int rebuiltCount = 0;
        for (int index = INDICATOR_LOOKBACK_CANDLES - 1; index < rawCandles.size(); index++) {
            List<MarketCandleRawEntity> window = rawCandles.subList(index - (INDICATOR_LOOKBACK_CANDLES - 1), index + 1);
            if (!isContiguous(window, interval)) {
                continue;
            }

            MarketCandleRawEntity latestRaw = rawCandles.get(index);
            if (latestRaw.getCloseTime() == null || latestRaw.getCloseTime().isBefore(rebuildFromSnapshotTime)) {
                continue;
            }

            List<Candle> candles = toCandles(window);
            persistOrRefresh(symbol, interval, latestRaw, candles);
            rebuiltCount++;
        }

        return rebuiltCount;
    }

    private Instant determineQueryStartOpenTime(
            Instant historyWindowStart,
            MarketIndicatorSnapshotEntity latestSnapshot,
            CandleInterval interval
    ) {
        if (latestSnapshot == null || latestSnapshot.getLatestCandleOpenTime() == null) {
            return historyWindowStart;
        }

        Instant overlapStart = latestSnapshot.getLatestCandleOpenTime().minus(
                interval.duration().multipliedBy(INDICATOR_LOOKBACK_CANDLES - 1L)
        );
        return overlapStart.isBefore(historyWindowStart) ? historyWindowStart : overlapStart;
    }

    private Instant determineRebuildFromSnapshotTime(
            MarketIndicatorSnapshotEntity latestSnapshot,
            CandleInterval interval
    ) {
        if (latestSnapshot == null || latestSnapshot.getSnapshotTime() == null) {
            return Instant.MIN;
        }

        return latestSnapshot.getSnapshotTime().minus(interval.duration().multipliedBy(SNAPSHOT_REBUILD_OVERLAP));
    }

    private boolean isContiguous(List<MarketCandleRawEntity> rawCandles, CandleInterval interval) {
        for (int index = 1; index < rawCandles.size(); index++) {
            Instant previousOpenTime = rawCandles.get(index - 1).getOpenTime();
            Instant currentOpenTime = rawCandles.get(index).getOpenTime();
            if (previousOpenTime == null || currentOpenTime == null) {
                return false;
            }
            if (!previousOpenTime.plus(interval.duration()).equals(currentOpenTime)) {
                return false;
            }
        }
        return true;
    }

    private List<Candle> toCandles(List<MarketCandleRawEntity> rawCandles) {
        List<Candle> candles = new ArrayList<>(rawCandles.size());
        for (MarketCandleRawEntity raw : rawCandles) {
            candles.add(new Candle(
                    raw.getOpenTime(),
                    raw.getCloseTime(),
                    raw.getOpenPrice(),
                    raw.getHighPrice(),
                    raw.getLowPrice(),
                    raw.getClosePrice(),
                    raw.getVolume()
            ));
        }
        return candles;
    }

    private void persistOrRefresh(
            String symbol,
            CandleInterval interval,
            MarketCandleRawEntity latestRaw,
            List<Candle> candles
    ) {
        Candle latestCandle = candles.get(candles.size() - 1);
        MarketIndicatorSnapshotEntity existingEntity = marketIndicatorSnapshotRepository
                .findTopBySymbolAndIntervalValueAndSnapshotTimeOrderByIdDesc(
                        symbol,
                        interval.value(),
                        latestCandle.closeTime()
                )
                .orElse(null);

        String sourceDataVersion = buildSourceDataVersion(interval, latestRaw, candles.size());
        BigDecimal currentPrice = latestCandle.close();
        BigDecimal ma20 = movingAverageCalculator.calculate(candles, 20).value();
        BigDecimal ma60 = movingAverageCalculator.calculate(candles, 60).value();
        BigDecimal ma120 = movingAverageCalculator.calculate(candles, 120).value();
        BigDecimal rsi14 = rsiCalculator.calculate(candles, 14).value();
        var macd = macdCalculator.calculate(candles);
        BigDecimal atr14 = atrCalculator.calculate(candles, 14).value();
        var bollingerBands20 = bollingerBandsCalculator.calculate(candles, 20);

        if (existingEntity == null) {
            marketIndicatorSnapshotRepository.save(
                    MarketIndicatorSnapshotEntity.builder()
                            .symbol(symbol)
                            .intervalValue(interval.value())
                            .snapshotTime(latestCandle.closeTime())
                            .latestCandleOpenTime(latestCandle.openTime())
                            .priceSourceEventTime(latestRaw.getCloseTime())
                            .sourceDataVersion(sourceDataVersion)
                            .currentPrice(currentPrice)
                            .ma20(ma20)
                            .ma60(ma60)
                            .ma120(ma120)
                            .rsi14(rsi14)
                            .macdLine(macd.macdLine())
                            .macdSignalLine(macd.signalLine())
                            .macdHistogram(macd.histogram())
                            .atr14(atr14)
                            .bollingerUpperBand(bollingerBands20.upperBand())
                            .bollingerMiddleBand(bollingerBands20.middleBand())
                            .bollingerLowerBand(bollingerBands20.lowerBand())
                            .build()
            );
            return;
        }

        existingEntity.refreshFromSnapshot(
                latestCandle.openTime(),
                latestRaw.getCloseTime(),
                sourceDataVersion,
                currentPrice,
                ma20,
                ma60,
                ma120,
                rsi14,
                macd.macdLine(),
                macd.signalLine(),
                macd.histogram(),
                atr14,
                bollingerBands20.upperBand(),
                bollingerBands20.middleBand(),
                bollingerBands20.lowerBand()
        );
    }

    private String buildSourceDataVersion(
            CandleInterval interval,
            MarketCandleRawEntity latestRaw,
            int sampleCount
    ) {
        return "source=market_candle_raw"
                + ";interval=" + interval.value()
                + ";latestOpenTime=" + latestRaw.getOpenTime()
                + ";latestCloseTime=" + latestRaw.getCloseTime()
                + ";sampleCount=" + sampleCount;
    }
}

