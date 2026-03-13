package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.repository.MarketCandleRawRepository;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisMarketParticipationFactServiceTest {

    @Mock
    private MarketCandleRawRepository marketCandleRawRepository;

    @InjectMocks
    private AnalysisMarketParticipationFactService service;

    @Test
    void buildFactsBuildsShortTermParticipationFactsFromRawCandles() {
        MarketIndicatorSnapshotEntity snapshot = snapshot("BTCUSDT", "1h", Instant.parse("2026-03-09T23:00:00Z"));
        when(marketCandleRawRepository
                .findAllBySymbolAndIntervalValueAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualAndValidationStatusOrderByOpenTimeAsc(
                        eq("BTCUSDT"),
                        eq("1h"),
                        any(),
                        eq(snapshot.getLatestCandleOpenTime()),
                        eq(RawDataValidationStatus.VALID)
                ))
                .thenReturn(candles(snapshot.getLatestCandleOpenTime(), CandleInterval.ONE_HOUR, 48));

        List<String> facts = service.buildFacts(snapshot, AnalysisReportType.SHORT_TERM);

        assertThat(facts).hasSize(3);
        assertThat(facts).anySatisfy(fact -> assertThat(fact).contains("최근 3h 기준", "거래대금은 직전 동일 구간 대비", "체결 수는 직전 동일 구간 대비", "taker buy 비중은"));
        assertThat(facts).anySatisfy(fact -> assertThat(fact).contains("최근 6h 기준"));
        assertThat(facts).anySatisfy(fact -> assertThat(fact).contains("최근 24h 기준"));
    }

    @Test
    void buildFactsBuildsMidTermParticipationFactsFromRawCandles() {
        MarketIndicatorSnapshotEntity snapshot = snapshot("BTCUSDT", "4h", Instant.parse("2026-03-09T20:00:00Z"));
        when(marketCandleRawRepository
                .findAllBySymbolAndIntervalValueAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualAndValidationStatusOrderByOpenTimeAsc(
                        eq("BTCUSDT"),
                        eq("4h"),
                        any(),
                        eq(snapshot.getLatestCandleOpenTime()),
                        eq(RawDataValidationStatus.VALID)
                ))
                .thenReturn(candles(snapshot.getLatestCandleOpenTime(), CandleInterval.FOUR_HOUR, 360));

        List<String> facts = service.buildFacts(snapshot, AnalysisReportType.MID_TERM);

        assertThat(facts).hasSize(3);
        assertThat(facts).anySatisfy(fact -> assertThat(fact).contains("최근 3d 기준"));
        assertThat(facts).anySatisfy(fact -> assertThat(fact).contains("최근 7d 기준"));
        assertThat(facts).anySatisfy(fact -> assertThat(fact).contains("최근 30d 기준"));
    }

    @Test
    void buildFactsBuildsLongTermParticipationFactsFromRawCandles() {
        MarketIndicatorSnapshotEntity snapshot = snapshot("BTCUSDT", "1d", Instant.parse("2026-03-09T00:00:00Z"));
        when(marketCandleRawRepository
                .findAllBySymbolAndIntervalValueAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualAndValidationStatusOrderByOpenTimeAsc(
                        eq("BTCUSDT"),
                        eq("1d"),
                        any(),
                        eq(snapshot.getLatestCandleOpenTime()),
                        eq(RawDataValidationStatus.VALID)
                ))
                .thenReturn(candles(snapshot.getLatestCandleOpenTime(), CandleInterval.ONE_DAY, 360));

        List<String> facts = service.buildFacts(snapshot, AnalysisReportType.LONG_TERM);

        assertThat(facts).hasSize(3);
        assertThat(facts).anySatisfy(fact -> assertThat(fact).contains("최근 30d 기준"));
        assertThat(facts).anySatisfy(fact -> assertThat(fact).contains("최근 90d 기준"));
        assertThat(facts).anySatisfy(fact -> assertThat(fact).contains("최근 180d 기준"));
    }

    private MarketIndicatorSnapshotEntity snapshot(String symbol, String intervalValue, Instant latestOpenTime) {
        return MarketIndicatorSnapshotEntity.builder()
                                            .symbol(symbol)
                                            .intervalValue(intervalValue)
                                            .snapshotTime(latestOpenTime.plusSeconds(1))
                                            .latestCandleOpenTime(latestOpenTime)
                                            .currentPrice(new BigDecimal("87500"))
                                            .build();
    }

    private List<MarketCandleRawEntity> candles(Instant latestOpenTime, CandleInterval interval, int count) {
        Instant startOpenTime = latestOpenTime.minus(interval.duration().multipliedBy(count - 1L));
        List<MarketCandleRawEntity> candles = new ArrayList<>();
        Instant openTime = startOpenTime;
        for (int index = 0; index < count; index++) {
            boolean inCurrentHalf = index >= count / 2;
            BigDecimal openPrice = inCurrentHalf
                    ? new BigDecimal("87000").add(BigDecimal.valueOf(index - (count / 2L)).multiply(new BigDecimal("20")))
                    : new BigDecimal("85000").add(BigDecimal.valueOf(index).multiply(new BigDecimal("10")));
            BigDecimal closePrice = openPrice.add(inCurrentHalf ? new BigDecimal("15") : new BigDecimal("5"));
            candles.add(MarketCandleRawEntity.builder()
                                             .source("binance")
                                             .symbol("BTCUSDT")
                                             .intervalValue(interval.value())
                                             .openTime(openTime)
                                             .closeTime(openTime.plus(interval.duration()).minusMillis(1))
                                             .openPrice(openPrice)
                                             .highPrice(closePrice.add(new BigDecimal("5")))
                                             .lowPrice(openPrice.subtract(new BigDecimal("5")))
                                             .closePrice(closePrice)
                                             .volume(inCurrentHalf ? new BigDecimal("120") : new BigDecimal("100"))
                                             .quoteAssetVolume(inCurrentHalf ? new BigDecimal("12500000") : new BigDecimal("10000000"))
                                             .numberOfTrades(inCurrentHalf ? 13500L : 11000L)
                                             .takerBuyBaseAssetVolume(inCurrentHalf ? new BigDecimal("68") : new BigDecimal("50"))
                                             .takerBuyQuoteAssetVolume(inCurrentHalf ? new BigDecimal("7100000") : new BigDecimal("5000000"))
                                             .collectedTime(openTime.plus(interval.duration()))
                                             .validationStatus(RawDataValidationStatus.VALID)
                                             .rawPayload("{}")
                                             .build());
            openTime = openTime.plus(interval.duration());
        }
        return candles;
    }
}
