package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketCandleRawCoverageStatus;
import com.aicoinassist.batch.domain.market.entity.MarketCandleRawEntity;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.repository.MarketCandleRawRepository;
import com.aicoinassist.batch.infrastructure.client.binance.BinanceApiClient;
import com.aicoinassist.batch.infrastructure.client.binance.validator.BinanceKlineResponseValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
}

