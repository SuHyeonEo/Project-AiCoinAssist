package com.aicoinassist.batch.domain.market.repository;

import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
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
}
