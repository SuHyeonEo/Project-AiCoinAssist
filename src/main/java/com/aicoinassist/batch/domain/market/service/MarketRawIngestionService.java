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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketRawIngestionService {

    private static final String BINANCE_SOURCE = "BINANCE";
    private static final int DEFAULT_CANDLE_LIMIT = 120;

    private final BinanceApiClient binanceApiClient;
    private final BinanceAggregateTradeResponseValidator aggregateTradeResponseValidator;
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

        BinanceAggregateTradeResponse aggregateTradeResponse = binanceApiClient.getLatestAggregateTrade(symbol);
        RawDataValidationResult priceValidation = aggregateTradeResponseValidator.validate(aggregateTradeResponse);
        persistOrRefreshPriceRaw(symbol, collectedTime, aggregateTradeResponse, priceValidation);

        List<BinanceKlineResponse> klineResponses = binanceApiClient.getKlines(symbol, interval.value(), candleLimit);
        RawDataValidationResult sequenceValidation = klineResponseValidator.validateSequence(klineResponses);

        List<MarketCandleRawEntity> candleEntities = klineResponses.isEmpty()
                ? persistEmptyCandleSnapshot(symbol, interval, collectedTime, sequenceValidation)
                : persistOrRefreshCandles(symbol, interval, collectedTime, klineResponses, sequenceValidation);

        int invalidCandleCount = (int) candleEntities.stream()
                                                     .filter(entity -> entity.getValidationStatus() == RawDataValidationStatus.INVALID)
                                                     .count();

        return new MarketRawIngestionResult(
                symbol,
                interval,
                collectedTime,
                priceValidation.status(),
                klineResponses.size(),
                invalidCandleCount
        );
    }

    private MarketPriceRawEntity persistOrRefreshPriceRaw(
            String symbol,
            Instant collectedTime,
            BinanceAggregateTradeResponse response,
            RawDataValidationResult validation
    ) {
        Instant sourceEventTime = toInstant(response == null ? null : response.tradeTime());
        BigDecimal price = parseDecimal(response == null ? null : response.price());
        String rawPayload = serialize(response);

        if (sourceEventTime == null) {
            return marketPriceRawRepository.save(
                    MarketPriceRawEntity.builder()
                                        .source(BINANCE_SOURCE)
                                        .symbol(symbol)
                                        .sourceEventTime(null)
                                        .collectedTime(collectedTime)
                                        .validationStatus(validation.status())
                                        .validationDetails(validation.details())
                                        .price(price)
                                        .rawPayload(rawPayload)
                                        .build()
            );
        }

        MarketPriceRawEntity existingEntity = marketPriceRawRepository
                .findTopBySourceAndSymbolAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
                        BINANCE_SOURCE,
                        symbol,
                        sourceEventTime
                )
                .orElse(null);

        if (existingEntity == null) {
            return marketPriceRawRepository.save(
                    MarketPriceRawEntity.builder()
                                        .source(BINANCE_SOURCE)
                                        .symbol(symbol)
                                        .sourceEventTime(sourceEventTime)
                                        .collectedTime(collectedTime)
                                        .validationStatus(validation.status())
                                        .validationDetails(validation.details())
                                        .price(price)
                                        .rawPayload(rawPayload)
                                        .build()
            );
        }

        existingEntity.refreshFromIngestion(
                collectedTime,
                validation.status(),
                validation.details(),
                price,
                rawPayload
        );
        return existingEntity;
    }

    private List<MarketCandleRawEntity> persistEmptyCandleSnapshot(
            String symbol,
            CandleInterval interval,
            Instant collectedTime,
            RawDataValidationResult validation
    ) {
        List<MarketCandleRawEntity> emptyEntities =
                List.of(toEmptyMarketCandleRawEntity(symbol, interval, collectedTime, validation));

        marketCandleRawRepository.saveAll(emptyEntities);
        return emptyEntities;
    }

    private List<MarketCandleRawEntity> persistOrRefreshCandles(
            String symbol,
            CandleInterval interval,
            Instant collectedTime,
            List<BinanceKlineResponse> klineResponses,
            RawDataValidationResult sequenceValidation
    ) {
        Map<Instant, MarketCandleRawEntity> existingByOpenTime = loadExistingCandlesByOpenTime(symbol, interval, klineResponses);
        List<MarketCandleRawEntity> candleEntities = new ArrayList<>(klineResponses.size());
        List<MarketCandleRawEntity> newEntities = new ArrayList<>();

        for (BinanceKlineResponse response : klineResponses) {
            RawDataValidationResult validation = combineValidation(
                    klineResponseValidator.validateItem(response),
                    sequenceValidation
            );

            Instant openTime = toInstant(response == null ? null : response.openTime());
            MarketCandleRawEntity existingEntity = existingByOpenTime.get(openTime);

            if (existingEntity == null) {
                MarketCandleRawEntity newEntity = toMarketCandleRawEntity(
                        symbol,
                        interval,
                        collectedTime,
                        response,
                        validation
                );
                newEntities.add(newEntity);
                candleEntities.add(newEntity);
                continue;
            }

            refreshMarketCandleRawEntity(existingEntity, collectedTime, response, validation);
            candleEntities.add(existingEntity);
        }

        if (!newEntities.isEmpty()) {
            marketCandleRawRepository.saveAll(newEntities);
        }

        return candleEntities;
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

    private void refreshMarketCandleRawEntity(
            MarketCandleRawEntity entity,
            Instant collectedTime,
            BinanceKlineResponse response,
            RawDataValidationResult validation
    ) {
        entity.refreshFromIngestion(
                toInstant(response == null ? null : response.closeTime()),
                parseDecimal(response == null ? null : response.open()),
                parseDecimal(response == null ? null : response.high()),
                parseDecimal(response == null ? null : response.low()),
                parseDecimal(response == null ? null : response.close()),
                parseDecimal(response == null ? null : response.volume()),
                collectedTime,
                validation.status(),
                validation.details(),
                serialize(response == null ? null : response.rawValues())
        );
    }

    private Map<Instant, MarketCandleRawEntity> loadExistingCandlesByOpenTime(
            String symbol,
            CandleInterval interval,
            List<BinanceKlineResponse> klineResponses
    ) {
        List<Instant> openTimes = klineResponses.stream()
                                                .map(response -> toInstant(response == null ? null : response.openTime()))
                                                .filter(Objects::nonNull)
                                                .distinct()
                                                .toList();

        if (openTimes.isEmpty()) {
            return Map.of();
        }

        return marketCandleRawRepository.findAllBySourceAndSymbolAndIntervalValueAndOpenTimeIn(
                                       BINANCE_SOURCE,
                                       symbol,
                                       interval.value(),
                                       openTimes
                               )
                               .stream()
                               .filter(entity -> entity.getOpenTime() != null)
                               .collect(Collectors.toMap(
                                       MarketCandleRawEntity::getOpenTime,
                                       Function.identity(),
                                       this::selectMoreRecentEntity
                               ));
    }

    private MarketCandleRawEntity selectMoreRecentEntity(
            MarketCandleRawEntity left,
            MarketCandleRawEntity right
    ) {
        return Comparator
                .comparing(MarketCandleRawEntity::getCollectedTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(MarketCandleRawEntity::getId, Comparator.nullsLast(Comparator.naturalOrder()))
                .compare(left, right) >= 0
                ? left
                : right;
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
