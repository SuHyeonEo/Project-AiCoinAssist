package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketPriceRawEntity;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class RawTableConstraintTest {

    @Autowired
    private MarketPriceRawRepository marketPriceRawRepository;

    @Autowired
    private MarketCandleRawRepository marketCandleRawRepository;

    @Autowired
    private MarketIndicatorSnapshotRepository marketIndicatorSnapshotRepository;

    @Test
    void marketPriceRawRejectsDuplicateSourceSymbolAndSourceEventTime() {
        Instant sourceEventTime = Instant.parse("2026-03-09T00:00:00Z");

        marketPriceRawRepository.saveAndFlush(priceRaw(sourceEventTime, "87500.12"));

        assertThatThrownBy(() -> marketPriceRawRepository.saveAndFlush(priceRaw(sourceEventTime, "87510.12")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void marketCandleRawRejectsDuplicateSourceSymbolIntervalAndOpenTime() {
        Instant openTime = Instant.parse("2026-03-09T00:00:00Z");

        marketCandleRawRepository.saveAndFlush(candleRaw(openTime, "10", "12", "9", "11", "100"));

        assertThatThrownBy(() -> marketCandleRawRepository.saveAndFlush(
                candleRaw(openTime, "11", "13", "10", "12", "110")
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void marketIndicatorSnapshotRejectsDuplicateSymbolIntervalAndSnapshotTime() {
        Instant snapshotTime = Instant.parse("2026-03-09T00:59:59Z");

        marketIndicatorSnapshotRepository.saveAndFlush(indicatorSnapshot(snapshotTime, "87500.12"));

        assertThatThrownBy(() -> marketIndicatorSnapshotRepository.saveAndFlush(
                indicatorSnapshot(snapshotTime, "87510.12")
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    private MarketPriceRawEntity priceRaw(Instant sourceEventTime, String price) {
        return MarketPriceRawEntity.builder()
                                   .source("BINANCE")
                                   .symbol("BTCUSDT")
                                   .sourceEventTime(sourceEventTime)
                                   .collectedTime(Instant.parse("2026-03-09T00:01:00Z"))
                                   .validationStatus(RawDataValidationStatus.VALID)
                                   .price(new BigDecimal(price))
                                   .rawPayload("{\"price\":\"" + price + "\"}")
                                   .build();
    }

    private MarketCandleRawEntity candleRaw(
            Instant openTime,
            String openPrice,
            String highPrice,
            String lowPrice,
            String closePrice,
            String volume
    ) {
        return MarketCandleRawEntity.builder()
                                    .source("BINANCE")
                                    .symbol("BTCUSDT")
                                    .intervalValue("1h")
                                    .openTime(openTime)
                                    .closeTime(openTime.plusSeconds(3599))
                                    .openPrice(new BigDecimal(openPrice))
                                    .highPrice(new BigDecimal(highPrice))
                                    .lowPrice(new BigDecimal(lowPrice))
                                    .closePrice(new BigDecimal(closePrice))
                                    .volume(new BigDecimal(volume))
                                    .collectedTime(Instant.parse("2026-03-09T00:01:00Z"))
                                    .validationStatus(RawDataValidationStatus.VALID)
                                    .rawPayload("[]")
                                    .build();
    }

    private MarketIndicatorSnapshotEntity indicatorSnapshot(
            Instant snapshotTime,
            String currentPrice
    ) {
        return MarketIndicatorSnapshotEntity.builder()
                                            .symbol("BTCUSDT")
                                            .intervalValue("1h")
                                            .snapshotTime(snapshotTime)
                                            .currentPrice(new BigDecimal(currentPrice))
                                            .ma20(new BigDecimal("10"))
                                            .ma60(new BigDecimal("11"))
                                            .ma120(new BigDecimal("12"))
                                            .rsi14(new BigDecimal("50"))
                                            .macdLine(new BigDecimal("1"))
                                            .macdSignalLine(new BigDecimal("0.5"))
                                            .macdHistogram(new BigDecimal("0.5"))
                                            .atr14(new BigDecimal("2"))
                                            .bollingerUpperBand(new BigDecimal("15"))
                                            .bollingerMiddleBand(new BigDecimal("10"))
                                            .bollingerLowerBand(new BigDecimal("5"))
                                            .build();
    }
}
