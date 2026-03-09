package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
import com.aicoinassist.batch.domain.market.entity.MarketContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketOpenInterestRawEntity;
import com.aicoinassist.batch.domain.market.entity.MarketPremiumIndexRawEntity;
import com.aicoinassist.batch.domain.market.entity.MarketPriceRawEntity;
import com.aicoinassist.batch.domain.market.entity.MarketWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
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

    @Autowired
    private MarketOpenInterestRawRepository marketOpenInterestRawRepository;

    @Autowired
    private MarketPremiumIndexRawRepository marketPremiumIndexRawRepository;

    @Autowired
    private MarketContextSnapshotRepository marketContextSnapshotRepository;

    @Autowired
    private MarketWindowSummarySnapshotRepository marketWindowSummarySnapshotRepository;

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

    @Test
    void marketOpenInterestRawRejectsDuplicateSourceSymbolAndSourceEventTime() {
        Instant sourceEventTime = Instant.parse("2026-03-10T00:59:00Z");

        marketOpenInterestRawRepository.saveAndFlush(openInterestRaw(sourceEventTime, "12345.67"));

        assertThatThrownBy(() -> marketOpenInterestRawRepository.saveAndFlush(
                openInterestRaw(sourceEventTime, "12346.67")
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void marketPremiumIndexRawRejectsDuplicateSourceSymbolAndSourceEventTime() {
        Instant sourceEventTime = Instant.parse("2026-03-10T00:59:30Z");

        marketPremiumIndexRawRepository.saveAndFlush(premiumIndexRaw(sourceEventTime, "87500.12"));

        assertThatThrownBy(() -> marketPremiumIndexRawRepository.saveAndFlush(
                premiumIndexRaw(sourceEventTime, "87510.12")
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void marketContextSnapshotRejectsDuplicateSymbolAndSnapshotTime() {
        Instant snapshotTime = Instant.parse("2026-03-10T00:59:30Z");

        marketContextSnapshotRepository.saveAndFlush(contextSnapshot(snapshotTime, "12345.67"));

        assertThatThrownBy(() -> marketContextSnapshotRepository.saveAndFlush(
                contextSnapshot(snapshotTime, "12346.67")
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void marketWindowSummarySnapshotRejectsDuplicateSymbolIntervalWindowTypeAndWindowEndTime() {
        Instant windowEndTime = Instant.parse("2026-03-10T00:59:59Z");

        marketWindowSummarySnapshotRepository.saveAndFlush(windowSummary(windowEndTime, "87500.12"));

        assertThatThrownBy(() -> marketWindowSummarySnapshotRepository.saveAndFlush(
                windowSummary(windowEndTime, "87510.12")
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
                                            .latestCandleOpenTime(snapshotTime.minusSeconds(3600))
                                            .priceSourceEventTime(snapshotTime.minusSeconds(30))
                                            .sourceDataVersion(
                                                    "snapshotTime=" + snapshotTime
                                                            + ";latestCandleOpenTime=" + snapshotTime.minusSeconds(3600)
                                                            + ";priceSourceEventTime=" + snapshotTime.minusSeconds(30)
                                            )
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

    private MarketOpenInterestRawEntity openInterestRaw(Instant sourceEventTime, String openInterest) {
        return MarketOpenInterestRawEntity.builder()
                                          .source("BINANCE")
                                          .symbol("BTCUSDT")
                                          .sourceEventTime(sourceEventTime)
                                          .collectedTime(sourceEventTime.plusSeconds(10))
                                          .validationStatus(RawDataValidationStatus.VALID)
                                          .openInterest(new BigDecimal(openInterest))
                                          .rawPayload("{\"openInterest\":\"" + openInterest + "\"}")
                                          .build();
    }

    private MarketPremiumIndexRawEntity premiumIndexRaw(Instant sourceEventTime, String markPrice) {
        return MarketPremiumIndexRawEntity.builder()
                                          .source("BINANCE")
                                          .symbol("BTCUSDT")
                                          .sourceEventTime(sourceEventTime)
                                          .collectedTime(sourceEventTime.plusSeconds(10))
                                          .validationStatus(RawDataValidationStatus.VALID)
                                          .markPrice(new BigDecimal(markPrice))
                                          .indexPrice(new BigDecimal("87480.02"))
                                          .lastFundingRate(new BigDecimal("0.00025"))
                                          .nextFundingTime(Instant.parse("2026-03-10T08:00:00Z"))
                                          .rawPayload("{\"markPrice\":\"" + markPrice + "\"}")
                                          .build();
    }

    private MarketContextSnapshotEntity contextSnapshot(Instant snapshotTime, String openInterest) {
        return MarketContextSnapshotEntity.builder()
                                          .symbol("BTCUSDT")
                                          .snapshotTime(snapshotTime)
                                          .openInterestSourceEventTime(snapshotTime.minusSeconds(30))
                                          .premiumIndexSourceEventTime(snapshotTime)
                                          .sourceDataVersion(
                                                  "openInterestSourceEventTime=" + snapshotTime.minusSeconds(30)
                                                          + ";premiumIndexSourceEventTime=" + snapshotTime
                                                          + ";nextFundingTime=2026-03-10T08:00:00Z"
                                          )
                                          .openInterest(new BigDecimal(openInterest))
                                          .markPrice(new BigDecimal("87500.12"))
                                          .indexPrice(new BigDecimal("87480.02"))
                                          .lastFundingRate(new BigDecimal("0.00025"))
                                          .nextFundingTime(Instant.parse("2026-03-10T08:00:00Z"))
                                          .markIndexBasisRate(new BigDecimal("0.02297893"))
                                          .build();
    }

    private MarketWindowSummarySnapshotEntity windowSummary(Instant windowEndTime, String currentPrice) {
        return MarketWindowSummarySnapshotEntity.builder()
                                                .symbol("BTCUSDT")
                                                .intervalValue("1h")
                                                .windowType(MarketWindowType.LAST_7D.name())
                                                .windowStartTime(windowEndTime.minusSeconds(7L * 24L * 3600L))
                                                .windowEndTime(windowEndTime)
                                                .sampleCount(168)
                                                .currentPrice(new BigDecimal(currentPrice))
                                                .windowHigh(new BigDecimal("91000.00"))
                                                .windowLow(new BigDecimal("83000.00"))
                                                .windowRange(new BigDecimal("8000.00"))
                                                .currentPositionInRange(new BigDecimal("0.68750000"))
                                                .distanceFromWindowHigh(new BigDecimal("0.03846154"))
                                                .reboundFromWindowLow(new BigDecimal("0.05421687"))
                                                .averageVolume(new BigDecimal("100.00000000"))
                                                .averageAtr(new BigDecimal("1450.00000000"))
                                                .currentVolume(new BigDecimal("122.00000000"))
                                                .currentAtr(new BigDecimal("1500.00000000"))
                                                .currentVolumeVsAverage(new BigDecimal("0.22000000"))
                                                .currentAtrVsAverage(new BigDecimal("0.03448276"))
                                                .sourceDataVersion("basis-key;windowType=LAST_7D")
                                                .build();
    }
}
