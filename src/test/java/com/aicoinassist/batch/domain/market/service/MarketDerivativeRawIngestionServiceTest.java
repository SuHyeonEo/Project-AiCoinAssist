package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketDerivativeSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketOpenInterestRawEntity;
import com.aicoinassist.batch.domain.market.entity.MarketPremiumIndexRawEntity;
import com.aicoinassist.batch.domain.market.repository.MarketOpenInterestRawRepository;
import com.aicoinassist.batch.domain.market.repository.MarketPremiumIndexRawRepository;
import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.binance.BinanceDerivativesClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketDerivativeRawIngestionServiceTest {

    @Mock
    private BinanceDerivativesClient binanceDerivativesClient;

    @Mock
    private MarketOpenInterestRawRepository marketOpenInterestRawRepository;

    @Mock
    private MarketPremiumIndexRawRepository marketPremiumIndexRawRepository;

    @Test
    void ingestRefreshesExistingDerivativeRawRowsWhenKeysMatch() {
        MarketDerivativeRawIngestionService service = new MarketDerivativeRawIngestionService(
                binanceDerivativesClient,
                marketOpenInterestRawRepository,
                marketPremiumIndexRawRepository
        );

        MarketDerivativeSnapshot snapshot = snapshot();
        MarketOpenInterestRawEntity existingOpenInterest = MarketOpenInterestRawEntity.builder()
                                                                                      .source("BINANCE")
                                                                                      .symbol("BTCUSDT")
                                                                                      .sourceEventTime(snapshot.openInterestSourceEventTime())
                                                                                      .collectedTime(Instant.parse("2026-03-10T00:00:00Z"))
                                                                                      .validationStatus(snapshot.openInterestValidation().status())
                                                                                      .openInterest(new BigDecimal("10000"))
                                                                                      .rawPayload("{\"openInterest\":\"10000\"}")
                                                                                      .build();
        MarketPremiumIndexRawEntity existingPremiumIndex = MarketPremiumIndexRawEntity.builder()
                                                                                       .source("BINANCE")
                                                                                       .symbol("BTCUSDT")
                                                                                       .sourceEventTime(snapshot.premiumIndexSourceEventTime())
                                                                                       .collectedTime(Instant.parse("2026-03-10T00:00:00Z"))
                                                                                       .validationStatus(snapshot.premiumIndexValidation().status())
                                                                                       .markPrice(new BigDecimal("87000"))
                                                                                       .indexPrice(new BigDecimal("86990"))
                                                                                       .lastFundingRate(new BigDecimal("0.0001"))
                                                                                       .nextFundingTime(Instant.parse("2026-03-10T08:00:00Z"))
                                                                                       .rawPayload("{\"markPrice\":\"87000\"}")
                                                                                       .build();

        when(binanceDerivativesClient.fetchSnapshot("BTCUSDT")).thenReturn(snapshot);
        when(marketOpenInterestRawRepository.findTopBySourceAndSymbolAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
                "BINANCE",
                "BTCUSDT",
                snapshot.openInterestSourceEventTime()
        )).thenReturn(Optional.of(existingOpenInterest));
        when(marketPremiumIndexRawRepository.findTopBySourceAndSymbolAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
                "BINANCE",
                "BTCUSDT",
                snapshot.premiumIndexSourceEventTime()
        )).thenReturn(Optional.of(existingPremiumIndex));

        service.ingest("BTCUSDT");

        verify(marketOpenInterestRawRepository, never()).save(any(MarketOpenInterestRawEntity.class));
        verify(marketPremiumIndexRawRepository, never()).save(any(MarketPremiumIndexRawEntity.class));
        assertThat(existingOpenInterest.getOpenInterest()).isEqualByComparingTo("12345.67890000");
        assertThat(existingPremiumIndex.getLastFundingRate()).isEqualByComparingTo("0.00025000");
        assertThat(existingPremiumIndex.getMarkPrice()).isEqualByComparingTo("87500.12000000");
    }

    @Test
    void ingestPersistsNewDerivativeRawRowsWhenKeysDoNotExist() {
        MarketDerivativeRawIngestionService service = new MarketDerivativeRawIngestionService(
                binanceDerivativesClient,
                marketOpenInterestRawRepository,
                marketPremiumIndexRawRepository
        );

        MarketDerivativeSnapshot snapshot = snapshot();

        when(binanceDerivativesClient.fetchSnapshot("BTCUSDT")).thenReturn(snapshot);
        when(marketOpenInterestRawRepository.findTopBySourceAndSymbolAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
                "BINANCE",
                "BTCUSDT",
                snapshot.openInterestSourceEventTime()
        )).thenReturn(Optional.empty());
        when(marketPremiumIndexRawRepository.findTopBySourceAndSymbolAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
                "BINANCE",
                "BTCUSDT",
                snapshot.premiumIndexSourceEventTime()
        )).thenReturn(Optional.empty());

        service.ingest("BTCUSDT");

        verify(marketOpenInterestRawRepository).save(any(MarketOpenInterestRawEntity.class));
        verify(marketPremiumIndexRawRepository).save(any(MarketPremiumIndexRawEntity.class));
    }

    private MarketDerivativeSnapshot snapshot() {
        return new MarketDerivativeSnapshot(
                "BTCUSDT",
                Instant.parse("2026-03-10T00:59:00Z"),
                RawDataValidationResult.valid(),
                new BigDecimal("12345.67890000"),
                "{\"openInterest\":\"12345.67890000\"}",
                Instant.parse("2026-03-10T00:59:30Z"),
                RawDataValidationResult.valid(),
                new BigDecimal("87500.12000000"),
                new BigDecimal("87480.02000000"),
                new BigDecimal("0.00025000"),
                Instant.parse("2026-03-10T08:00:00Z"),
                "{\"markPrice\":\"87500.12000000\"}"
        );
    }
}
