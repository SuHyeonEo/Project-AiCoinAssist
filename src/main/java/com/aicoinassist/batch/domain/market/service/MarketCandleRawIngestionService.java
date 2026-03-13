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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketCandleRawIngestionService {

    private static final String BINANCE_SOURCE = "BINANCE";
    private static final int KLINE_LIMIT_CAP = 1000;

    private final BinanceApiClient binanceApiClient;
    private final BinanceKlineResponseValidator binanceKlineResponseValidator;
    private final MarketCandleRawRepository marketCandleRawRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Transactional(readOnly = true)
    public MarketCandleRawCoverageStatus assessCoverage(
            String symbol,
            CandleInterval interval,
            int requiredCandleCount
    ) {
        Instant now = clock.instant();
        Instant expectedLatestOpenTime = interval.latestClosedOpenTime(now);
        if (expectedLatestOpenTime == null) {
            return new MarketCandleRawCoverageStatus(
                    symbol,
                    interval,
                    null,
                    null,
                    null,
                    requiredCandleCount,
                    0,
                    requiredCandleCount,
                    requiredCandleCount,
                    false
            );
        }

        Instant requiredWindowStartOpenTime = expectedLatestOpenTime.minus(
                interval.duration().multipliedBy(Math.max(0L, requiredCandleCount - 1L))
        );
        MarketCandleRawEntity latestStored = marketCandleRawRepository
                .findTopBySymbolAndIntervalValueAndValidationStatusOrderByOpenTimeDescIdDesc(
                        symbol,
                        interval.value(),
                        RawDataValidationStatus.VALID
                )
                .orElse(null);
        long availableValidCandleCount = marketCandleRawRepository
                .countBySymbolAndIntervalValueAndOpenTimeGreaterThanEqualAndOpenTimeLessThanEqualAndValidationStatus(
                        symbol,
                        interval.value(),
                        requiredWindowStartOpenTime,
                        expectedLatestOpenTime,
                        RawDataValidationStatus.VALID
                );
        int missingCandleCount = Math.max(0, requiredCandleCount - Math.toIntExact(Math.min(Integer.MAX_VALUE, availableValidCandleCount)));
        int tailGapCandleCount = latestStored == null || latestStored.getOpenTime() == null
                ? requiredCandleCount
                : tailGap(interval, latestStored.getOpenTime(), expectedLatestOpenTime);

        return new MarketCandleRawCoverageStatus(
                symbol,
                interval,
                expectedLatestOpenTime,
                latestStored == null ? null : latestStored.getOpenTime(),
                requiredWindowStartOpenTime,
                requiredCandleCount,
                availableValidCandleCount,
                missingCandleCount,
                tailGapCandleCount,
                missingCandleCount == 0 && tailGapCandleCount == 0
        );
    }

    @Transactional
    public MarketCandleRawCoverageStatus startupBackfill(
            String symbol,
            CandleInterval interval,
            int requiredCandleCount
    ) {
        MarketCandleRawCoverageStatus coverage = assessCoverage(symbol, interval, requiredCandleCount);
        if (coverage.sufficientlyCovered()) {
            return coverage;
        }

        fetchAndPersistClosedCandles(symbol, interval, requiredCandleCount, clock.instant());
        return assessCoverage(symbol, interval, requiredCandleCount);
    }

    @Transactional
    public MarketCandleRawCoverageStatus gapFill(
            String symbol,
            CandleInterval interval,
            int requiredCandleCount,
            int overlapCount
    ) {
        MarketCandleRawCoverageStatus coverage = assessCoverage(symbol, interval, requiredCandleCount);
        int fetchCount = determineGapFillFetchCount(coverage, overlapCount);
        if (fetchCount <= 0) {
            return coverage;
        }

        fetchAndPersistClosedCandles(symbol, interval, fetchCount, clock.instant());
        return assessCoverage(symbol, interval, requiredCandleCount);
    }

    private int determineGapFillFetchCount(
            MarketCandleRawCoverageStatus coverage,
            int overlapCount
    ) {
        if (coverage.expectedLatestOpenTime() == null) {
            return 0;
        }
        if (coverage.missingCandleCount() > coverage.tailGapCandleCount()) {
            return Math.min(coverage.requiredCandleCount(), KLINE_LIMIT_CAP);
        }
        if (coverage.tailGapCandleCount() > 0) {
            return Math.min(coverage.tailGapCandleCount() + overlapCount, KLINE_LIMIT_CAP);
        }
        return Math.min(Math.max(overlapCount, 1), KLINE_LIMIT_CAP);
    }

    private void fetchAndPersistClosedCandles(
            String symbol,
            CandleInterval interval,
            int requestedLimit,
            Instant now
    ) {
        Instant latestClosedCloseTime = interval.latestClosedCloseTime(now);
        if (latestClosedCloseTime == null) {
            return;
        }

        int limit = Math.min(requestedLimit, KLINE_LIMIT_CAP);
        List<BinanceKlineResponse> responses = binanceApiClient.getKlines(
                symbol,
                interval.value(),
                limit,
                latestClosedCloseTime.toEpochMilli()
        );
        RawDataValidationResult sequenceValidation = binanceKlineResponseValidator.validateSequence(responses);
        if (!sequenceValidation.isValid()) {
            throw new IllegalStateException("Binance kline sequence is invalid: " + sequenceValidation.details());
        }

        Instant collectedTime = clock.instant();
        for (BinanceKlineResponse response : responses) {
            persistOrRefresh(symbol, interval, response, collectedTime);
        }
    }

    private void persistOrRefresh(
            String symbol,
            CandleInterval interval,
            BinanceKlineResponse response,
            Instant collectedTime
    ) {
        Instant openTime = response.openTime() == null ? null : Instant.ofEpochMilli(response.openTime());
        Instant closeTime = response.closeTime() == null ? null : Instant.ofEpochMilli(response.closeTime());
        RawDataValidationResult validation = binanceKlineResponseValidator.validateItem(response);

        MarketCandleRawEntity existingEntity = openTime == null
                ? null
                : marketCandleRawRepository
                        .findTopBySourceAndSymbolAndIntervalValueAndOpenTimeOrderByCollectedTimeDescIdDesc(
                                BINANCE_SOURCE,
                                symbol,
                                interval.value(),
                                openTime
                        )
                        .orElse(null);

        BigDecimal openPrice = parseDecimal(response.open());
        BigDecimal highPrice = parseDecimal(response.high());
        BigDecimal lowPrice = parseDecimal(response.low());
        BigDecimal closePrice = parseDecimal(response.close());
        BigDecimal volume = parseDecimal(response.volume());
        BigDecimal quoteAssetVolume = parseDecimal(response.quoteAssetVolume());
        BigDecimal takerBuyBaseAssetVolume = parseDecimal(response.takerBuyBaseAssetVolume());
        BigDecimal takerBuyQuoteAssetVolume = parseDecimal(response.takerBuyQuoteAssetVolume());
        String rawPayload = serializeRawPayload(response.rawValues());

        if (existingEntity == null) {
            marketCandleRawRepository.save(
                    MarketCandleRawEntity.builder()
                            .source(BINANCE_SOURCE)
                            .symbol(symbol)
                            .intervalValue(interval.value())
                            .openTime(openTime)
                            .closeTime(closeTime)
                            .openPrice(openPrice)
                            .highPrice(highPrice)
                            .lowPrice(lowPrice)
                            .closePrice(closePrice)
                            .volume(volume)
                            .quoteAssetVolume(quoteAssetVolume)
                            .numberOfTrades(response.numberOfTrades())
                            .takerBuyBaseAssetVolume(takerBuyBaseAssetVolume)
                            .takerBuyQuoteAssetVolume(takerBuyQuoteAssetVolume)
                            .collectedTime(collectedTime)
                            .validationStatus(validation.status())
                            .validationDetails(validation.details())
                            .rawPayload(rawPayload)
                            .build()
            );
            return;
        }

        existingEntity.refreshFromIngestion(
                closeTime,
                openPrice,
                highPrice,
                lowPrice,
                closePrice,
                volume,
                quoteAssetVolume,
                response.numberOfTrades(),
                takerBuyBaseAssetVolume,
                takerBuyQuoteAssetVolume,
                collectedTime,
                validation.status(),
                validation.details(),
                rawPayload
        );
    }

    private int tailGap(CandleInterval interval, Instant latestStoredOpenTime, Instant expectedLatestOpenTime) {
        Duration gap = Duration.between(latestStoredOpenTime, expectedLatestOpenTime);
        if (gap.isNegative() || gap.isZero()) {
            return 0;
        }
        return Math.toIntExact(gap.getSeconds() / interval.duration().getSeconds());
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String serializeRawPayload(List<String> rawValues) {
        try {
            return objectMapper.writeValueAsString(rawValues);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize Binance kline raw payload.", exception);
        }
    }
}

