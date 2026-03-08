package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketRawIngestionResult;
import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
import com.aicoinassist.batch.domain.market.entity.MarketPriceRawEntity;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.repository.MarketCandleRawRepository;
import com.aicoinassist.batch.domain.market.repository.MarketPriceRawRepository;
import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.binance.BinanceApiClient;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceAggregateTradeResponse;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceKlineResponse;
import com.aicoinassist.batch.infrastructure.client.binance.validator.BinanceAggregateTradeResponseValidator;
import com.aicoinassist.batch.infrastructure.client.binance.validator.BinanceKlineResponseValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketRawIngestionServiceTest {

    @Mock
    private BinanceApiClient binanceApiClient;

    @Mock
    private BinanceAggregateTradeResponseValidator aggregateTradeResponseValidator;

    @Mock
    private BinanceKlineResponseValidator klineResponseValidator;

    @Mock
    private MarketPriceRawRepository marketPriceRawRepository;

    @Mock
    private MarketCandleRawRepository marketCandleRawRepository;

    @Test
    void ingestStoresPlaceholderRecordWhenKlineResponseIsEmpty() {
        MarketRawIngestionService service = new MarketRawIngestionService(
                binanceApiClient,
                aggregateTradeResponseValidator,
                klineResponseValidator,
                marketPriceRawRepository,
                marketCandleRawRepository,
                new ObjectMapper()
        );

        BinanceAggregateTradeResponse aggregateTradeResponse = aggregateTrade(1L, "87500.12", "0.10", 1000L);

        when(binanceApiClient.getLatestAggregateTrade("BTCUSDT")).thenReturn(aggregateTradeResponse);
        when(aggregateTradeResponseValidator.validate(aggregateTradeResponse)).thenReturn(RawDataValidationResult.valid());
        when(marketPriceRawRepository.findTopBySourceAndSymbolAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
                "BINANCE",
                "BTCUSDT",
                Instant.ofEpochMilli(1000L)
        )).thenReturn(Optional.empty());
        when(marketPriceRawRepository.save(any(MarketPriceRawEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(binanceApiClient.getKlines("BTCUSDT", "1h", 120)).thenReturn(List.of());
        when(klineResponseValidator.validateSequence(List.of()))
                .thenReturn(RawDataValidationResult.invalid("Kline response list is empty."));
        when(marketCandleRawRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MarketRawIngestionResult result = service.ingest("BTCUSDT", CandleInterval.ONE_HOUR);

        ArgumentCaptor<List<MarketCandleRawEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(marketCandleRawRepository).saveAll(captor.capture());

        List<MarketCandleRawEntity> savedEntities = captor.getValue();

        assertThat(result.candleCount()).isZero();
        assertThat(result.invalidCandleCount()).isEqualTo(1);
        assertThat(savedEntities).hasSize(1);
        assertThat(savedEntities.get(0).getValidationStatus()).isEqualTo(RawDataValidationStatus.INVALID);
        assertThat(savedEntities.get(0).getRawPayload()).isEqualTo("[]");
        assertThat(savedEntities.get(0).getIntervalValue()).isEqualTo(CandleInterval.ONE_HOUR.value());
    }

    @Test
    void ingestRefreshesExistingCandleInsteadOfInsertingDuplicate() {
        MarketRawIngestionService service = new MarketRawIngestionService(
                binanceApiClient,
                aggregateTradeResponseValidator,
                klineResponseValidator,
                marketPriceRawRepository,
                marketCandleRawRepository,
                new ObjectMapper()
        );

        BinanceAggregateTradeResponse aggregateTradeResponse = aggregateTrade(1L, "87500.12", "0.10", 1000L);
        BinanceKlineResponse klineResponse = kline(
                1000L,
                1999L,
                "10",
                "12",
                "9",
                "11",
                "100"
        );

        MarketCandleRawEntity existingEntity = MarketCandleRawEntity.builder()
                                                                    .source("BINANCE")
                                                                    .symbol("BTCUSDT")
                                                                    .intervalValue("1h")
                                                                    .openTime(Instant.ofEpochMilli(1000L))
                                                                    .closeTime(Instant.ofEpochMilli(1999L))
                                                                    .openPrice(new BigDecimal("10"))
                                                                    .highPrice(new BigDecimal("10"))
                                                                    .lowPrice(new BigDecimal("9"))
                                                                    .closePrice(new BigDecimal("10"))
                                                                    .volume(new BigDecimal("90"))
                                                                    .collectedTime(Instant.parse("2026-03-08T00:00:00Z"))
                                                                    .validationStatus(RawDataValidationStatus.INVALID)
                                                                    .validationDetails("old data")
                                                                    .rawPayload("[\"old\"]")
                                                                    .build();

        when(binanceApiClient.getLatestAggregateTrade("BTCUSDT")).thenReturn(aggregateTradeResponse);
        when(aggregateTradeResponseValidator.validate(aggregateTradeResponse)).thenReturn(RawDataValidationResult.valid());
        when(marketPriceRawRepository.findTopBySourceAndSymbolAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
                "BINANCE",
                "BTCUSDT",
                Instant.ofEpochMilli(1000L)
        )).thenReturn(Optional.empty());
        when(marketPriceRawRepository.save(any(MarketPriceRawEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(binanceApiClient.getKlines("BTCUSDT", "1h", 120)).thenReturn(List.of(klineResponse));
        when(klineResponseValidator.validateSequence(List.of(klineResponse))).thenReturn(RawDataValidationResult.valid());
        when(klineResponseValidator.validateItem(klineResponse)).thenReturn(RawDataValidationResult.valid());
        when(marketCandleRawRepository.findAllBySourceAndSymbolAndIntervalValueAndOpenTimeIn(
                eq("BINANCE"),
                eq("BTCUSDT"),
                eq("1h"),
                anyCollection()
        )).thenReturn(List.of(existingEntity));

        MarketRawIngestionResult result = service.ingest("BTCUSDT", CandleInterval.ONE_HOUR);

        verify(marketCandleRawRepository, never()).saveAll(any());

        assertThat(result.candleCount()).isEqualTo(1);
        assertThat(result.invalidCandleCount()).isZero();
        assertThat(existingEntity.getHighPrice()).isEqualByComparingTo("12");
        assertThat(existingEntity.getClosePrice()).isEqualByComparingTo("11");
        assertThat(existingEntity.getVolume()).isEqualByComparingTo("100");
        assertThat(existingEntity.getValidationStatus()).isEqualTo(RawDataValidationStatus.VALID);
        assertThat(existingEntity.getValidationDetails()).isNull();
        assertThat(existingEntity.getRawPayload()).contains("1000");
    }

    @Test
    void ingestRefreshesExistingPriceRawWhenAggregateTradeTimeMatches() {
        MarketRawIngestionService service = new MarketRawIngestionService(
                binanceApiClient,
                aggregateTradeResponseValidator,
                klineResponseValidator,
                marketPriceRawRepository,
                marketCandleRawRepository,
                new ObjectMapper()
        );

        BinanceAggregateTradeResponse aggregateTradeResponse = aggregateTrade(1L, "87500.12", "0.10", 1000L);
        MarketPriceRawEntity existingEntity = MarketPriceRawEntity.builder()
                                                                  .source("BINANCE")
                                                                  .symbol("BTCUSDT")
                                                                  .sourceEventTime(Instant.ofEpochMilli(1000L))
                                                                  .collectedTime(Instant.parse("2026-03-08T00:00:00Z"))
                                                                  .validationStatus(RawDataValidationStatus.INVALID)
                                                                  .validationDetails("old data")
                                                                  .price(new BigDecimal("87000"))
                                                                  .rawPayload("{\"price\":\"87000\"}")
                                                                  .build();

        when(binanceApiClient.getLatestAggregateTrade("BTCUSDT")).thenReturn(aggregateTradeResponse);
        when(aggregateTradeResponseValidator.validate(aggregateTradeResponse)).thenReturn(RawDataValidationResult.valid());
        when(marketPriceRawRepository.findTopBySourceAndSymbolAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
                "BINANCE",
                "BTCUSDT",
                Instant.ofEpochMilli(1000L)
        )).thenReturn(Optional.of(existingEntity));
        when(binanceApiClient.getKlines("BTCUSDT", "1h", 120)).thenReturn(List.of());
        when(klineResponseValidator.validateSequence(List.of()))
                .thenReturn(RawDataValidationResult.invalid("Kline response list is empty."));
        when(marketCandleRawRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MarketRawIngestionResult result = service.ingest("BTCUSDT", CandleInterval.ONE_HOUR);

        verify(marketPriceRawRepository, never()).save(any(MarketPriceRawEntity.class));

        assertThat(result.priceValidationStatus()).isEqualTo(RawDataValidationStatus.VALID);
        assertThat(existingEntity.getPrice()).isEqualByComparingTo("87500.12");
        assertThat(existingEntity.getValidationStatus()).isEqualTo(RawDataValidationStatus.VALID);
        assertThat(existingEntity.getValidationDetails()).isNull();
        assertThat(existingEntity.getRawPayload()).contains("87500.12");
    }

    private BinanceKlineResponse kline(
            Long openTime,
            Long closeTime,
            String open,
            String high,
            String low,
            String close,
            String volume
    ) {
        return new BinanceKlineResponse(
                openTime,
                open,
                high,
                low,
                close,
                volume,
                closeTime,
                "0",
                10L,
                "0",
                "0",
                "0",
                List.of(
                        String.valueOf(openTime),
                        open,
                        high,
                        low,
                        close,
                        volume,
                        String.valueOf(closeTime),
                        "0",
                        "10",
                        "0",
                        "0",
                        "0"
                )
        );
    }

    private BinanceAggregateTradeResponse aggregateTrade(
            Long aggregateTradeId,
            String price,
            String quantity,
            Long tradeTime
    ) {
        return new BinanceAggregateTradeResponse(
                aggregateTradeId,
                price,
                quantity,
                1L,
                1L,
                tradeTime,
                false,
                true
        );
    }
}
