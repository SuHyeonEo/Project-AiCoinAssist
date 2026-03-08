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
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceKlineResponse;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceTickerPriceResponse;
import com.aicoinassist.batch.infrastructure.client.binance.validator.BinanceKlineResponseValidator;
import com.aicoinassist.batch.infrastructure.client.binance.validator.BinanceTickerPriceResponseValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketRawIngestionService {

    private static final String BINANCE_SOURCE = "BINANCE";
    private static final int DEFAULT_CANDLE_LIMIT = 120;

    private final BinanceApiClient binanceApiClient;
    private final BinanceTickerPriceResponseValidator tickerPriceResponseValidator;
    private final BinanceKlineResponseValidator klineResponseValidator;
    private final MarketPriceRawRepository marketPriceRawRepository;
    private final MarketCandleRawRepository marketCandleRawRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public MarketRawIngestionResult ingest(String symbol, CandleInterval interval) {
        return ingest(symbol, interval, DEFAULT_CANDLE_LIMIT);
    }

    @Transactional
    public MarketRawIngestionResult ingest(String symbol, CandleInterval interval, int candleLimit) {
        Instant collectedTime = Instant.now();

        BinanceTickerPriceResponse tickerResponse = binanceApiClient.getTickerPrice(symbol);
        RawDataValidationResult tickerValidation = tickerPriceResponseValidator.validate(symbol, tickerResponse);

        marketPriceRawRepository.save(
                MarketPriceRawEntity.builder()
                                    .source(BINANCE_SOURCE)
                                    .symbol(symbol)
                                    .sourceEventTime(null)
                                    .collectedTime(collectedTime)
                                    .validationStatus(tickerValidation.status())
                                    .validationDetails(tickerValidation.details())
                                    .price(parseDecimal(tickerResponse == null ? null : tickerResponse.price()))
                                    .rawPayload(serialize(tickerResponse))
                                    .build()
        );

        List<BinanceKlineResponse> klineResponses = binanceApiClient.getKlines(symbol, interval.value(), candleLimit);
        RawDataValidationResult sequenceValidation = klineResponseValidator.validateSequence(klineResponses);

        List<MarketCandleRawEntity> candleEntities = klineResponses.isEmpty()
                ? List.of(toEmptyMarketCandleRawEntity(symbol, interval, collectedTime, sequenceValidation))
                : klineResponses.stream()
                                .map(response -> toMarketCandleRawEntity(
                                        symbol,
                                        interval,
                                        collectedTime,
                                        response,
                                        combineValidation(
                                                klineResponseValidator.validateItem(response),
                                                sequenceValidation
                                        )
                                ))
                                .toList();

        marketCandleRawRepository.saveAll(candleEntities);

        int invalidCandleCount = (int) candleEntities.stream()
                                                     .filter(entity -> entity.getValidationStatus() == RawDataValidationStatus.INVALID)
                                                     .count();

        return new MarketRawIngestionResult(
                symbol,
                interval,
                collectedTime,
                tickerValidation.status(),
                klineResponses.size(),
                invalidCandleCount
        );
    }

    private MarketCandleRawEntity toMarketCandleRawEntity(
            String symbol,
            CandleInterval interval,
            Instant collectedTime,
            BinanceKlineResponse response,
            RawDataValidationResult validation
    ) {
        return MarketCandleRawEntity.builder()
                                    .source(BINANCE_SOURCE)
                                    .symbol(symbol)
                                    .intervalValue(interval.value())
                                    .openTime(toInstant(response == null ? null : response.openTime()))
                                    .closeTime(toInstant(response == null ? null : response.closeTime()))
                                    .openPrice(parseDecimal(response == null ? null : response.open()))
                                    .highPrice(parseDecimal(response == null ? null : response.high()))
                                    .lowPrice(parseDecimal(response == null ? null : response.low()))
                                    .closePrice(parseDecimal(response == null ? null : response.close()))
                                    .volume(parseDecimal(response == null ? null : response.volume()))
                                    .collectedTime(collectedTime)
                                    .validationStatus(validation.status())
                                    .validationDetails(validation.details())
                                    .rawPayload(serialize(response == null ? null : response.rawValues()))
                                    .build();
    }

    private MarketCandleRawEntity toEmptyMarketCandleRawEntity(
            String symbol,
            CandleInterval interval,
            Instant collectedTime,
            RawDataValidationResult validation
    ) {
        return MarketCandleRawEntity.builder()
                                    .source(BINANCE_SOURCE)
                                    .symbol(symbol)
                                    .intervalValue(interval.value())
                                    .collectedTime(collectedTime)
                                    .validationStatus(validation.status())
                                    .validationDetails(validation.details())
                                    .rawPayload(serialize(List.of()))
                                    .build();
    }

    private RawDataValidationResult combineValidation(
            RawDataValidationResult itemValidation,
            RawDataValidationResult sequenceValidation
    ) {
        if (itemValidation.isValid() && sequenceValidation.isValid()) {
            return RawDataValidationResult.valid();
        }

        if (!itemValidation.isValid() && !sequenceValidation.isValid()) {
            return RawDataValidationResult.invalid(itemValidation.details() + " | " + sequenceValidation.details());
        }

        return itemValidation.isValid() ? sequenceValidation : itemValidation;
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

    private Instant toInstant(Long epochMilli) {
        return epochMilli == null ? null : Instant.ofEpochMilli(epochMilli);
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize raw market payload.", exception);
        }
    }
}
