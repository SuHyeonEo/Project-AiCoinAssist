package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.indicator.calculator.AtrCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.BollingerBandsCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.MacdCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.MovingAverageCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.RsiCalculator;
import com.aicoinassist.batch.domain.indicator.dto.AtrResult;
import com.aicoinassist.batch.domain.indicator.dto.BollingerBandsResult;
import com.aicoinassist.batch.domain.indicator.dto.MacdResult;
import com.aicoinassist.batch.domain.indicator.dto.MovingAverageResult;
import com.aicoinassist.batch.domain.indicator.dto.RsiResult;
import com.aicoinassist.batch.domain.market.dto.Candle;
import com.aicoinassist.batch.domain.market.dto.MarketIndicatorSnapshot;
import com.aicoinassist.batch.domain.market.dto.MarketPriceSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.repository.MarketCandleRawRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketIndicatorSnapshotService {

    private static final int DEFAULT_CANDLE_LIMIT = 120;

    private final MarketCandleRawRepository marketCandleRawRepository;
    private final MarketAnalysisPriceService marketAnalysisPriceService;
    private final MovingAverageCalculator movingAverageCalculator;
    private final RsiCalculator rsiCalculator;
    private final MacdCalculator macdCalculator;
    private final AtrCalculator atrCalculator;
    private final BollingerBandsCalculator bollingerBandsCalculator;
    private final Clock clock;

    public MarketIndicatorSnapshot create(String symbol, CandleInterval interval) {
        return create(symbol, interval, DEFAULT_CANDLE_LIMIT);
    }

    public MarketIndicatorSnapshot create(String symbol, CandleInterval interval, int candleLimit) {
        List<MarketCandleRawEntity> rawCandles = loadContiguousClosedRawCandles(symbol, interval, candleLimit);
        List<Candle> candles = rawCandles.stream()
                .map(this::toCandle)
                .toList();
        MarketPriceSnapshot priceSnapshot = marketAnalysisPriceService.getLatestAnalysisPrice(symbol);

        MovingAverageResult ma20 = movingAverageCalculator.calculate(candles, 20);
        MovingAverageResult ma60 = movingAverageCalculator.calculate(candles, 60);
        MovingAverageResult ma120 = movingAverageCalculator.calculate(candles, 120);
        RsiResult rsi14 = rsiCalculator.calculate(candles, 14);
        MacdResult macd = macdCalculator.calculate(candles);
        AtrResult atr14 = atrCalculator.calculate(candles, 14);
        BollingerBandsResult bollingerBands20 = bollingerBandsCalculator.calculate(candles, 20);

        return new MarketIndicatorSnapshot(
                symbol,
                priceSnapshot,
                candles,
                ma20,
                ma60,
                ma120,
                rsi14,
                macd,
                atr14,
                bollingerBands20
        );
    }

    private List<MarketCandleRawEntity> loadContiguousClosedRawCandles(
            String symbol,
            CandleInterval interval,
            int candleLimit
    ) {
        Instant expectedLatestOpenTime = interval.latestClosedOpenTime(clock.instant());
        if (expectedLatestOpenTime == null) {
            throw new IllegalStateException("No closed raw candles are available yet for " + symbol + " " + interval.value());
        }

        Instant windowStartOpenTime = expectedLatestOpenTime.minus(interval.duration().multipliedBy(candleLimit - 1L));
        List<MarketCandleRawEntity> rawCandles = marketCandleRawRepository
                .findAllBySymbolAndIntervalValueAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualAndValidationStatusOrderByOpenTimeAsc(
                        symbol,
                        interval.value(),
                        windowStartOpenTime,
                        expectedLatestOpenTime,
                        RawDataValidationStatus.VALID
                );
        if (rawCandles.size() != candleLimit) {
            throw new IllegalStateException(
                    "Insufficient raw candles for indicator snapshot: symbol=%s interval=%s required=%s available=%s windowStart=%s latestExpected=%s"
                            .formatted(
                                    symbol,
                                    interval.value(),
                                    candleLimit,
                                    rawCandles.size(),
                                    windowStartOpenTime,
                                    expectedLatestOpenTime
                            )
            );
        }

        List<MarketCandleRawEntity> contiguous = latestContiguousWindow(rawCandles, interval, candleLimit);
        if (contiguous.size() != candleLimit
                || !windowStartOpenTime.equals(contiguous.get(0).getOpenTime())
                || !expectedLatestOpenTime.equals(contiguous.get(contiguous.size() - 1).getOpenTime())) {
            throw new IllegalStateException(
                    "Raw candle coverage has gaps for indicator snapshot: symbol=%s interval=%s required=%s windowStart=%s latestExpected=%s"
                            .formatted(symbol, interval.value(), candleLimit, windowStartOpenTime, expectedLatestOpenTime)
            );
        }

        return contiguous;
    }

    private List<MarketCandleRawEntity> latestContiguousWindow(
            List<MarketCandleRawEntity> rawCandles,
            CandleInterval interval,
            int candleLimit
    ) {
        List<MarketCandleRawEntity> contiguous = new ArrayList<>();
        for (MarketCandleRawEntity rawCandle : rawCandles) {
            if (rawCandle.getOpenTime() == null
                    || rawCandle.getCloseTime() == null
                    || rawCandle.getOpenPrice() == null
                    || rawCandle.getHighPrice() == null
                    || rawCandle.getLowPrice() == null
                    || rawCandle.getClosePrice() == null
                    || rawCandle.getVolume() == null) {
                contiguous.clear();
                continue;
            }

            if (contiguous.isEmpty()) {
                contiguous.add(rawCandle);
            } else {
                MarketCandleRawEntity previous = contiguous.get(contiguous.size() - 1);
                if (previous.getOpenTime().plus(interval.duration()).equals(rawCandle.getOpenTime())) {
                    contiguous.add(rawCandle);
                } else {
                    contiguous.clear();
                    contiguous.add(rawCandle);
                }
            }

            if (contiguous.size() > candleLimit) {
                contiguous.remove(0);
            }
        }

        return Collections.unmodifiableList(contiguous);
    }

    private Candle toCandle(MarketCandleRawEntity rawCandle) {
        return new Candle(
                rawCandle.getOpenTime(),
                rawCandle.getCloseTime(),
                rawCandle.getOpenPrice(),
                rawCandle.getHighPrice(),
                rawCandle.getLowPrice(),
                rawCandle.getClosePrice(),
                rawCandle.getVolume()
        );
    }
}
