package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.repository.MarketCandleRawRepository;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalysisMarketParticipationFactService {

    private static final int SCALE = 8;

    private final MarketCandleRawRepository marketCandleRawRepository;

    public List<String> buildFacts(MarketIndicatorSnapshotEntity snapshot, AnalysisReportType reportType) {
        if (snapshot == null || reportType == null || snapshot.getLatestCandleOpenTime() == null) {
            return List.of();
        }

        CandleInterval interval = intervalFor(reportType);
        List<ParticipationWindowSpec> specs = windowSpecs(reportType);
        int maxCandleCount = specs.stream()
                                  .mapToInt(ParticipationWindowSpec::candleCount)
                                  .max()
                                  .orElse(0);
        if (maxCandleCount <= 0) {
            return List.of();
        }

        Instant latestOpenTime = snapshot.getLatestCandleOpenTime();
        Instant earliestOpenTime = latestOpenTime.minus(interval.duration().multipliedBy((maxCandleCount * 2L) - 1L));
        List<MarketCandleRawEntity> candles = marketCandleRawRepository
                .findAllBySymbolAndIntervalValueAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualAndValidationStatusOrderByOpenTimeAsc(
                        snapshot.getSymbol(),
                        interval.value(),
                        earliestOpenTime,
                        latestOpenTime,
                        RawDataValidationStatus.VALID
                );
        if (candles.isEmpty()) {
            return List.of();
        }

        Map<Instant, MarketCandleRawEntity> candleByOpenTime = candles.stream()
                .collect(Collectors.toMap(MarketCandleRawEntity::getOpenTime, Function.identity(), (left, right) -> right));

        List<String> facts = new ArrayList<>();
        for (ParticipationWindowSpec spec : specs) {
            String fact = buildWindowFact(spec, interval.duration(), latestOpenTime, candleByOpenTime);
            if (fact != null && !fact.isBlank()) {
                facts.add(fact);
            }
        }
        return facts;
    }

    private String buildWindowFact(
            ParticipationWindowSpec spec,
            Duration candleDuration,
            Instant latestOpenTime,
            Map<Instant, MarketCandleRawEntity> candleByOpenTime
    ) {
        Instant currentStartOpenTime = latestOpenTime.minus(candleDuration.multipliedBy(spec.candleCount() - 1L));
        Instant previousEndOpenTime = currentStartOpenTime.minus(candleDuration);
        Instant previousStartOpenTime = previousEndOpenTime.minus(candleDuration.multipliedBy(spec.candleCount() - 1L));

        List<MarketCandleRawEntity> currentWindow = collectWindow(candleByOpenTime, currentStartOpenTime, spec.candleCount(), candleDuration);
        List<MarketCandleRawEntity> previousWindow = collectWindow(candleByOpenTime, previousStartOpenTime, spec.candleCount(), candleDuration);
        if (currentWindow.size() != spec.candleCount() || previousWindow.size() != spec.candleCount()) {
            return null;
        }

        BigDecimal currentQuoteVolume = sum(currentWindow.stream().map(MarketCandleRawEntity::getQuoteAssetVolume).toList());
        BigDecimal previousQuoteVolume = sum(previousWindow.stream().map(MarketCandleRawEntity::getQuoteAssetVolume).toList());
        BigDecimal currentTradeCount = sum(currentWindow.stream()
                                                        .map(MarketCandleRawEntity::getNumberOfTrades)
                                                        .map(value -> value == null ? null : BigDecimal.valueOf(value))
                                                        .toList());
        BigDecimal previousTradeCount = sum(previousWindow.stream()
                                                          .map(MarketCandleRawEntity::getNumberOfTrades)
                                                          .map(value -> value == null ? null : BigDecimal.valueOf(value))
                                                          .toList());
        BigDecimal currentTakerBuyQuoteVolume = sum(currentWindow.stream().map(MarketCandleRawEntity::getTakerBuyQuoteAssetVolume).toList());
        BigDecimal previousTakerBuyQuoteVolume = sum(previousWindow.stream().map(MarketCandleRawEntity::getTakerBuyQuoteAssetVolume).toList());
        BigDecimal currentTakerBuyRatio = ratio(currentTakerBuyQuoteVolume, currentQuoteVolume);
        BigDecimal previousTakerBuyRatio = ratio(previousTakerBuyQuoteVolume, previousQuoteVolume);
        BigDecimal takerBuyRatioDelta = currentTakerBuyRatio == null || previousTakerBuyRatio == null
                ? null
                : currentTakerBuyRatio.subtract(previousTakerBuyRatio);
        BigDecimal priceChangeRate = priceChangeRate(
                currentWindow.get(0).getOpenPrice(),
                currentWindow.get(currentWindow.size() - 1).getClosePrice()
        );

        List<String> components = gather(
                priceChangeRate == null
                        ? null
                        : "가격은 " + signedPercent(priceChangeRate),
                deltaRatio(currentQuoteVolume, previousQuoteVolume) == null
                        ? null
                        : "거래대금은 직전 동일 구간 대비 " + signedPercent(deltaRatio(currentQuoteVolume, previousQuoteVolume)),
                deltaRatio(currentTradeCount, previousTradeCount) == null
                        ? null
                        : "체결 수는 직전 동일 구간 대비 " + signedPercent(deltaRatio(currentTradeCount, previousTradeCount)),
                currentTakerBuyRatio == null
                        ? null
                        : "taker buy 비중은 " + percent(currentTakerBuyRatio)
                        + (takerBuyRatioDelta == null ? "" : " (직전 대비 " + signedPercentagePoint(takerBuyRatioDelta) + ")")
        );
        if (components.isEmpty()) {
            return null;
        }

        return spec.label() + " 기준 " + String.join(", ", components) + "입니다.";
    }

    private List<MarketCandleRawEntity> collectWindow(
            Map<Instant, MarketCandleRawEntity> candleByOpenTime,
            Instant startOpenTime,
            int candleCount,
            Duration candleDuration
    ) {
        List<MarketCandleRawEntity> candles = new ArrayList<>();
        Instant currentOpenTime = startOpenTime;
        for (int index = 0; index < candleCount; index++) {
            MarketCandleRawEntity candle = candleByOpenTime.get(currentOpenTime);
            if (candle == null) {
                return List.of();
            }
            candles.add(candle);
            currentOpenTime = currentOpenTime.plus(candleDuration);
        }
        return candles;
    }

    private List<ParticipationWindowSpec> windowSpecs(AnalysisReportType reportType) {
        return switch (reportType) {
            case SHORT_TERM -> List.of(
                    new ParticipationWindowSpec("최근 3h", 3),
                    new ParticipationWindowSpec("최근 6h", 6),
                    new ParticipationWindowSpec("최근 24h", 24)
            );
            case MID_TERM -> List.of(
                    new ParticipationWindowSpec("최근 3d", 18),
                    new ParticipationWindowSpec("최근 7d", 42),
                    new ParticipationWindowSpec("최근 30d", 180)
            );
            case LONG_TERM -> List.of(
                    new ParticipationWindowSpec("최근 30d", 30),
                    new ParticipationWindowSpec("최근 90d", 90),
                    new ParticipationWindowSpec("최근 180d", 180)
            );
        };
    }

    private CandleInterval intervalFor(AnalysisReportType reportType) {
        return switch (reportType) {
            case SHORT_TERM -> CandleInterval.ONE_HOUR;
            case MID_TERM -> CandleInterval.FOUR_HOUR;
            case LONG_TERM -> CandleInterval.ONE_DAY;
        };
    }

    private BigDecimal priceChangeRate(BigDecimal startPrice, BigDecimal endPrice) {
        if (startPrice == null || endPrice == null || startPrice.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return endPrice.subtract(startPrice)
                       .divide(startPrice, SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal deltaRatio(BigDecimal currentValue, BigDecimal previousValue) {
        if (currentValue == null || previousValue == null || previousValue.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return currentValue.subtract(previousValue)
                           .divide(previousValue, SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return numerator.divide(denominator, SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<BigDecimal> values) {
        return values.stream()
                     .filter(value -> value != null)
                     .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String signedPercent(BigDecimal value) {
        BigDecimal asPercent = value.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        if (asPercent.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + asPercent.toPlainString() + "%";
        }
        return asPercent.toPlainString() + "%";
    }

    private String percent(BigDecimal value) {
        return value.multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString() + "%";
    }

    private String signedPercentagePoint(BigDecimal value) {
        BigDecimal asPercent = value.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
        if (asPercent.compareTo(BigDecimal.ZERO) > 0) {
            return "+" + asPercent.toPlainString() + "%p";
        }
        return asPercent.toPlainString() + "%p";
    }

    private List<String> gather(String... values) {
        List<String> facts = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                facts.add(value);
            }
        }
        return facts;
    }

    private record ParticipationWindowSpec(String label, int candleCount) {
    }
}
