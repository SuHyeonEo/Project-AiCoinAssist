package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.entity.MarketPriceRawEntity;
import com.aicoinassist.batch.domain.market.repository.MarketPriceRawRepository;
import com.aicoinassist.batch.infrastructure.client.binance.BinanceApiClient;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceAggregateTradeResponse;
import com.aicoinassist.batch.infrastructure.client.binance.validator.BinanceAggregateTradeResponseValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketPriceRawIngestionServiceTest {

    @Mock
    private BinanceApiClient binanceApiClient;

    @Mock
    private MarketPriceRawRepository marketPriceRawRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void ingestLatestPriceSavesValidatedAggregateTrade() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-13T10:15:00Z"), ZoneOffset.UTC);
        MarketPriceRawIngestionService service = new MarketPriceRawIngestionService(
                binanceApiClient,
                new BinanceAggregateTradeResponseValidator(),
                marketPriceRawRepository,
                objectMapper,
                clock
        );

        BinanceAggregateTradeResponse response = new BinanceAggregateTradeResponse(
                1L,
                "87499.12",
                "0.25",
                1L,
                1L,
                Instant.parse("2026-03-13T10:14:55Z").toEpochMilli(),
                false,
                true
        );
        when(binanceApiClient.getLatestAggregateTrade("BTCUSDT")).thenReturn(response);
        when(marketPriceRawRepository.findTopBySourceAndSymbolAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
                "BINANCE",
                "BTCUSDT",
                Instant.parse("2026-03-13T10:14:55Z")
        )).thenReturn(Optional.empty());
        when(marketPriceRawRepository.saveAndFlush(any(MarketPriceRawEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MarketPriceRawEntity saved = service.ingestLatestPrice("BTCUSDT");

        assertThat(saved.getSymbol()).isEqualTo("BTCUSDT");
        assertThat(saved.getSourceEventTime()).isEqualTo(Instant.parse("2026-03-13T10:14:55Z"));
        assertThat(saved.getCollectedTime()).isEqualTo(Instant.parse("2026-03-13T10:15:00Z"));
        assertThat(saved.getPrice()).isEqualByComparingTo(new BigDecimal("87499.12"));
        assertThat(saved.getRawPayload()).contains("87499.12");
    }

    @Test
    void ingestLatestPriceReturnsExistingEntityWhenDuplicateInsertOccurs() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-13T10:16:00Z"), ZoneOffset.UTC);
        MarketPriceRawIngestionService service = new MarketPriceRawIngestionService(
                binanceApiClient,
                new BinanceAggregateTradeResponseValidator(),
                marketPriceRawRepository,
                objectMapper,
                clock
        );

        Instant sourceEventTime = Instant.parse("2026-03-13T10:15:59.989Z");
        BinanceAggregateTradeResponse response = new BinanceAggregateTradeResponse(
                2L,
                "87510.00",
                "0.31",
                1L,
                1L,
                sourceEventTime.toEpochMilli(),
                false,
                true
        );
        MarketPriceRawEntity existingEntity = MarketPriceRawEntity.builder()
                .source("BINANCE")
                .symbol("BTCUSDT")
                .sourceEventTime(sourceEventTime)
                .collectedTime(Instant.parse("2026-03-13T10:15:59.995Z"))
                .validationStatus(com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus.VALID)
                .validationDetails(null)
                .price(new BigDecimal("87510.00"))
                .rawPayload("{\"p\":\"87510.00\"}")
                .build();

        when(binanceApiClient.getLatestAggregateTrade("BTCUSDT")).thenReturn(response);
        when(marketPriceRawRepository.findTopBySourceAndSymbolAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
                "BINANCE",
                "BTCUSDT",
                sourceEventTime
        )).thenReturn(Optional.empty(), Optional.of(existingEntity));
        when(marketPriceRawRepository.saveAndFlush(any(MarketPriceRawEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        MarketPriceRawEntity loaded = service.ingestLatestPrice("BTCUSDT");

        assertThat(loaded).isSameAs(existingEntity);
        assertThat(loaded.getSourceEventTime()).isEqualTo(sourceEventTime);
    }
}
