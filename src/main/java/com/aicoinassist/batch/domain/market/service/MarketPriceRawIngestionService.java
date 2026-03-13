package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.entity.MarketPriceRawEntity;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.repository.MarketPriceRawRepository;
import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.binance.BinanceApiClient;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceAggregateTradeResponse;
import com.aicoinassist.batch.infrastructure.client.binance.validator.BinanceAggregateTradeResponseValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MarketPriceRawIngestionService {

    private static final String BINANCE_SOURCE = "BINANCE";

    private final BinanceApiClient binanceApiClient;
    private final BinanceAggregateTradeResponseValidator aggregateTradeResponseValidator;
    private final MarketPriceRawRepository marketPriceRawRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Transactional
    public MarketPriceRawEntity ingestLatestPrice(String symbol) {
        BinanceAggregateTradeResponse response = binanceApiClient.getLatestAggregateTrade(symbol);
        RawDataValidationResult validation = aggregateTradeResponseValidator.validate(response);
        if (response == null || response.tradeTime() == null) {
            throw new IllegalStateException("Latest aggregate trade is unavailable for " + symbol);
        }

        Instant sourceEventTime = Instant.ofEpochMilli(response.tradeTime());
        Instant collectedTime = clock.instant();
        BigDecimal price = parseDecimal(response.price());
        String rawPayload = serialize(response);

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

    private String serialize(BinanceAggregateTradeResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize Binance aggregate trade payload.", exception);
        }
    }
}
