package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.Candle;
import com.aicoinassist.batch.domain.market.dto.MarketCandidateLevelSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelLabel;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelSourceType;
import com.aicoinassist.batch.domain.market.enumtype.MarketCandidateLevelType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
class MarketCandidateLevelSnapshotServiceTest {

    @Test
    void createAllIncludesPivotLevelsAndEnrichedStructureFacts() {
        MarketCandidateLevelSnapshotService service = new MarketCandidateLevelSnapshotService();
        MarketIndicatorSnapshotEntity snapshot = snapshot();
        List<Candle> candles = candles();

        List<MarketCandidateLevelSnapshot> result = service.createAll(snapshot, candles);

        assertThat(result).extracting(MarketCandidateLevelSnapshot::levelType)
                          .contains(MarketCandidateLevelType.SUPPORT, MarketCandidateLevelType.RESISTANCE);
        assertThat(result).extracting(MarketCandidateLevelSnapshot::levelLabel)
                          .contains(MarketCandidateLevelLabel.PIVOT_LOW, MarketCandidateLevelLabel.PIVOT_HIGH);
        assertThat(result).filteredOn(level -> level.levelLabel() == MarketCandidateLevelLabel.PIVOT_LOW)
                          .allSatisfy(level -> {
                              assertThat(level.sourceType()).isEqualTo(MarketCandidateLevelSourceType.PIVOT_LEVEL);
                              assertThat(level.referenceTime()).isBefore(level.snapshotTime());
                              assertThat(level.reactionCount()).isPositive();
                              assertThat(level.clusterSize()).isPositive();
                              assertThat(level.strengthScore()).isGreaterThan(BigDecimal.ZERO);
                              assertThat(level.triggerFacts()).anySatisfy(fact -> assertThat(fact).contains("Reaction count"));
                          });
        assertThat(result).allSatisfy(level -> assertThat(level.sourceDataVersion()).contains("referenceTime="));
    }

    private MarketIndicatorSnapshotEntity snapshot() {
        return MarketIndicatorSnapshotEntity.builder()
                                            .symbol("BTCUSDT")
                                            .intervalValue("1h")
                                            .snapshotTime(Instant.parse("2026-03-10T00:59:59Z"))
                                            .latestCandleOpenTime(Instant.parse("2026-03-09T23:59:59Z"))
                                            .priceSourceEventTime(Instant.parse("2026-03-10T00:59:30Z"))
                                            .sourceDataVersion("basis-key")
                                            .currentPrice(new BigDecimal("87500"))
                                            .ma20(new BigDecimal("87000"))
                                            .ma60(new BigDecimal("86200"))
                                            .ma120(new BigDecimal("85000"))
                                            .rsi14(new BigDecimal("62"))
                                            .macdLine(new BigDecimal("120"))
                                            .macdSignalLine(new BigDecimal("100"))
                                            .macdHistogram(new BigDecimal("20"))
                                            .atr14(new BigDecimal("1500"))
                                            .bollingerUpperBand(new BigDecimal("88500"))
                                            .bollingerMiddleBand(new BigDecimal("87000"))
                                            .bollingerLowerBand(new BigDecimal("85500"))
                                            .build();
    }

    private List<Candle> candles() {
        return List.of(
                candle("2026-03-09T18:59:59Z", "87000", "87200", "86850", "87150", "100"),
                candle("2026-03-09T19:59:59Z", "87150", "87400", "86950", "87350", "120"),
                candle("2026-03-09T20:59:59Z", "87350", "87900", "87200", "87800", "150"),
                candle("2026-03-09T21:59:59Z", "87800", "87650", "86400", "86650", "210"),
                candle("2026-03-09T22:59:59Z", "86650", "87100", "86500", "86900", "160"),
                candle("2026-03-09T23:59:59Z", "86900", "88600", "86850", "88400", "240"),
                candle("2026-03-10T00:59:59Z", "88400", "88500", "87300", "87500", "190")
        );
    }

    private Candle candle(
            String openTime,
            String openPrice,
            String highPrice,
            String lowPrice,
            String closePrice,
            String volume
    ) {
        Instant open = Instant.parse(openTime);
        return new Candle(
                open,
                open.plusSeconds(3599),
                new BigDecimal(openPrice),
                new BigDecimal(highPrice),
                new BigDecimal(lowPrice),
                new BigDecimal(closePrice),
                new BigDecimal(volume)
        );
    }
}
