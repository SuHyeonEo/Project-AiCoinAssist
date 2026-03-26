package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketCandleRawCoverageStatus;
import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.repository.MarketCandleRawRepository;
import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.binance.BinanceApiClient;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceKlineResponse;
import com.aicoinassist.batch.infrastructure.client.binance.validator.BinanceKlineResponseValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketCandleRawIngestionServiceTest {

    @Mock
    private BinanceApiClient binanceApiClient;

    @Mock
    private BinanceKlineResponseValidator binanceKlineResponseValidator;

    @Mock
    private MarketCandleRawRepository marketCandleRawRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void assessCoverageReportsMissingHistoryAndTailGap() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-13T10:15:00Z"), ZoneOffset.UTC);
        MarketCandleRawIngestionService service = new MarketCandleRawIngestionService(
                binanceApiClient,
                binanceKlineResponseValidator,
                marketCandleRawRepository,
                objectMapper,
                clock
        );

        when(marketCandleRawRepository.findTopBySymbolAndIntervalValueAndValidationStatusOrderByOpenTimeDescIdDesc(
                "BTCUSDT",
                "1h",
                RawDataValidationStatus.VALID
        )).thenReturn(Optional.of(MarketCandleRawEntity.builder()
                .source("BINANCE")
                .symbol("BTCUSDT")
                .intervalValue("1h")
                .openTime(Instant.parse("2026-03-13T07:00:00Z"))
                .closeTime(Instant.parse("2026-03-13T08:00:00Z"))
                .collectedTime(Instant.parse("2026-03-13T08:00:10Z"))
                .validationStatus(RawDataValidationStatus.VALID)
                .rawPayload("[\"raw\"]")
                .build()));
        when(marketCandleRawRepository.countBySymbolAndIntervalValueAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualAndValidationStatus(
                "BTCUSDT",
                "1h",
                Instant.parse("2026-03-13T05:00:00Z"),
                Instant.parse("2026-03-13T09:00:00Z"),
                RawDataValidationStatus.VALID
        )).thenReturn(3L);

        MarketCandleRawCoverageStatus status = service.assessCoverage("BTCUSDT", CandleInterval.ONE_HOUR, 5);

        assertThat(status.expectedLatestOpenTime()).isEqualTo(Instant.parse("2026-03-13T09:00:00Z"));
        assertThat(status.requiredWindowStartOpenTime()).isEqualTo(Instant.parse("2026-03-13T05:00:00Z"));
        assertThat(status.availableValidCandleCount()).isEqualTo(3);
        assertThat(status.missingCandleCount()).isEqualTo(2);
        assertThat(status.tailGapCandleCount()).isEqualTo(2);
        assertThat(status.sufficientlyCovered()).isFalse();
    }

    @Test
    void startupBackfillRequestsOnlyClosedCandles() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-13T10:15:00Z"), ZoneOffset.UTC);
        MarketCandleRawIngestionService service = new MarketCandleRawIngestionService(
                binanceApiClient,
                binanceKlineResponseValidator,
                marketCandleRawRepository,
                objectMapper,
                clock
        );

        Instant requiredStart = Instant.parse("2026-03-13T05:00:00Z");
        Instant expectedLatestOpen = Instant.parse("2026-03-13T09:00:00Z");
        Instant latestStoredOpen = Instant.parse("2026-03-13T08:00:00Z");
        BinanceKlineResponse response = new BinanceKlineResponse(
                expectedLatestOpen.toEpochMilli(),
                "100",
                "110",
                "90",
                "105",
                "10",
                Instant.parse("2026-03-13T09:59:59.999Z").toEpochMilli(),
                "1000",
                12L,
                "5",
                "500",
                "0",
                List.of("raw")
        );

        when(marketCandleRawRepository.findTopBySymbolAndIntervalValueAndValidationStatusOrderByOpenTimeDescIdDesc(
                "BTCUSDT",
                "1h",
                RawDataValidationStatus.VALID
        )).thenReturn(
                Optional.of(MarketCandleRawEntity.builder()
                        .source("BINANCE")
                        .symbol("BTCUSDT")
                        .intervalValue("1h")
                        .openTime(latestStoredOpen)
                        .closeTime(Instant.parse("2026-03-13T08:59:59.999Z"))
                        .collectedTime(Instant.parse("2026-03-13T09:00:10Z"))
                        .validationStatus(RawDataValidationStatus.VALID)
                        .rawPayload("[\"raw\"]")
                        .build()),
                Optional.of(MarketCandleRawEntity.builder()
                        .source("BINANCE")
                        .symbol("BTCUSDT")
                        .intervalValue("1h")
                        .openTime(latestStoredOpen)
                        .closeTime(Instant.parse("2026-03-13T08:59:59.999Z"))
                        .collectedTime(Instant.parse("2026-03-13T09:00:10Z"))
                        .validationStatus(RawDataValidationStatus.VALID)
                        .rawPayload("[\"raw\"]")
                        .build())
        );
        when(marketCandleRawRepository.countBySymbolAndIntervalValueAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualAndValidationStatus(
                "BTCUSDT",
                "1h",
                requiredStart,
                expectedLatestOpen,
                RawDataValidationStatus.VALID
        )).thenReturn(4L, 5L);
        when(binanceApiClient.getKlines(
                "BTCUSDT",
                "1h",
                5,
                Instant.parse("2026-03-13T09:59:59.999Z").toEpochMilli()
        )).thenReturn(List.of(response));
        when(binanceKlineResponseValidator.validateSequence(any())).thenReturn(RawDataValidationResult.valid());
        when(binanceKlineResponseValidator.validateItem(response)).thenReturn(RawDataValidationResult.valid());
        when(marketCandleRawRepository.findTopBySourceAndSymbolAndIntervalValueAndOpenTimeOrderByCollectedTimeDescIdDesc(
                "BINANCE",
                "BTCUSDT",
                "1h",
                expectedLatestOpen
        )).thenReturn(Optional.empty());
        when(marketCandleRawRepository.saveAndFlush(any(MarketCandleRawEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MarketCandleRawCoverageStatus status = service.startupBackfill("BTCUSDT", CandleInterval.ONE_HOUR, 5);

        assertThat(status.availableValidCandleCount()).isEqualTo(5);
        verify(binanceApiClient).getKlines(
                eq("BTCUSDT"),
                eq("1h"),
                eq(5),
                eq(Instant.parse("2026-03-13T09:59:59.999Z").toEpochMilli())
        );
    }

    @Test
    void startupBackfillLoadsExistingEntityWhenDuplicateInsertOccurs() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-13T10:15:00Z"), ZoneOffset.UTC);
        MarketCandleRawIngestionService service = new MarketCandleRawIngestionService(
                binanceApiClient,
                binanceKlineResponseValidator,
                marketCandleRawRepository,
                objectMapper,
                clock
        );

        Instant expectedLatestOpen = Instant.parse("2026-03-13T09:00:00Z");
        BinanceKlineResponse response = new BinanceKlineResponse(
                expectedLatestOpen.toEpochMilli(),
                "100",
                "110",
                "90",
                "105",
                "10",
                Instant.parse("2026-03-13T09:59:59.999Z").toEpochMilli(),
                "1000",
                12L,
                "5",
                "500",
                "0",
                List.of("raw")
        );
        MarketCandleRawEntity existingEntity = MarketCandleRawEntity.builder()
                .source("BINANCE")
                .symbol("BTCUSDT")
                .intervalValue("1h")
                .openTime(expectedLatestOpen)
                .closeTime(Instant.parse("2026-03-13T09:59:59.999Z"))
                .collectedTime(Instant.parse("2026-03-13T10:00:10Z"))
                .validationStatus(RawDataValidationStatus.VALID)
                .rawPayload("[\"raw\"]")
                .build();

        when(marketCandleRawRepository.findTopBySymbolAndIntervalValueAndValidationStatusOrderByOpenTimeDescIdDesc(
                "BTCUSDT",
                "1h",
                RawDataValidationStatus.VALID
        )).thenReturn(Optional.empty(), Optional.of(existingEntity));
        when(marketCandleRawRepository.countBySymbolAndIntervalValueAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualAndValidationStatus(
                eq("BTCUSDT"),
                eq("1h"),
                any(),
                eq(expectedLatestOpen),
                eq(RawDataValidationStatus.VALID)
        )).thenReturn(0L, 1L);
        when(binanceApiClient.getKlines(
                "BTCUSDT",
                "1h",
                1,
                Instant.parse("2026-03-13T09:59:59.999Z").toEpochMilli()
        )).thenReturn(List.of(response));
        when(binanceKlineResponseValidator.validateSequence(any())).thenReturn(RawDataValidationResult.valid());
        when(binanceKlineResponseValidator.validateItem(response)).thenReturn(RawDataValidationResult.valid());
        when(marketCandleRawRepository.findTopBySourceAndSymbolAndIntervalValueAndOpenTimeOrderByCollectedTimeDescIdDesc(
                "BINANCE",
                "BTCUSDT",
                "1h",
                expectedLatestOpen
        )).thenReturn(Optional.empty(), Optional.of(existingEntity));
        when(marketCandleRawRepository.saveAndFlush(any(MarketCandleRawEntity.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        MarketCandleRawCoverageStatus status = service.startupBackfill("BTCUSDT", CandleInterval.ONE_HOUR, 1);

        assertThat(status.availableValidCandleCount()).isEqualTo(1);
    }
}
