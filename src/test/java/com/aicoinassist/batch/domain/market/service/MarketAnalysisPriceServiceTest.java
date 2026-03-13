package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.config.ExternalRawIngestionProperties;
import com.aicoinassist.batch.domain.market.entity.MarketPriceRawEntity;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.repository.MarketPriceRawRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketAnalysisPriceServiceTest {

    @Mock
    private MarketPriceRawRepository marketPriceRawRepository;

    @Test
    void getLatestAnalysisPriceReturnsLatestFreshRawPrice() {
        MarketAnalysisPriceService service = new MarketAnalysisPriceService(
                marketPriceRawRepository,
                new ExternalRawIngestionProperties(
                        true,
                        true,
                        30000L,
                        0L,
                        900000L,
                        true,
                        300000L,
                        0L,
                        true,
                        3,
                        true,
                        3600000L,
                        0L,
                        true,
                        21600000L,
                        0L,
                        true,
                        43200000L,
                        0L
                ),
                Clock.fixed(Instant.parse("2026-03-13T10:15:00Z"), ZoneOffset.UTC)
        );

        when(marketPriceRawRepository.findTopBySymbolAndValidationStatusOrderBySourceEventTimeDescIdDesc(
                "BTCUSDT",
                RawDataValidationStatus.VALID
        )).thenReturn(Optional.of(
                MarketPriceRawEntity.builder()
                        .source("BINANCE")
                        .symbol("BTCUSDT")
                        .sourceEventTime(Instant.parse("2026-03-13T10:14:55Z"))
                        .collectedTime(Instant.parse("2026-03-13T10:15:00Z"))
                        .validationStatus(RawDataValidationStatus.VALID)
                        .price(new BigDecimal("87510.12"))
                        .rawPayload("{\"price\":\"87510.12\"}")
                        .build()
        ));

        var price = service.getLatestAnalysisPrice("BTCUSDT");

        assertThat(price.price()).isEqualByComparingTo("87510.12");
        assertThat(price.sourceEventTime()).isEqualTo(Instant.parse("2026-03-13T10:14:55Z"));
    }

    @Test
    void getLatestAnalysisPriceRejectsStaleRawPrice() {
        MarketAnalysisPriceService service = new MarketAnalysisPriceService(
                marketPriceRawRepository,
                new ExternalRawIngestionProperties(
                        true,
                        true,
                        30000L,
                        0L,
                        60000L,
                        true,
                        300000L,
                        0L,
                        true,
                        3,
                        true,
                        3600000L,
                        0L,
                        true,
                        21600000L,
                        0L,
                        true,
                        43200000L,
                        0L
                ),
                Clock.fixed(Instant.parse("2026-03-13T10:15:00Z"), ZoneOffset.UTC)
        );

        when(marketPriceRawRepository.findTopBySymbolAndValidationStatusOrderBySourceEventTimeDescIdDesc(
                "BTCUSDT",
                RawDataValidationStatus.VALID
        )).thenReturn(Optional.of(
                MarketPriceRawEntity.builder()
                        .source("BINANCE")
                        .symbol("BTCUSDT")
                        .sourceEventTime(Instant.parse("2026-03-13T10:10:00Z"))
                        .collectedTime(Instant.parse("2026-03-13T10:10:05Z"))
                        .validationStatus(RawDataValidationStatus.VALID)
                        .price(new BigDecimal("87510.12"))
                        .rawPayload("{\"price\":\"87510.12\"}")
                        .build()
        ));

        assertThatThrownBy(() -> service.getLatestAnalysisPrice("BTCUSDT"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stale");
    }
}
