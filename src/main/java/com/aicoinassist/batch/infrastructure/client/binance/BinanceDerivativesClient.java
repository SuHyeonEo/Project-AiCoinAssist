package com.aicoinassist.batch.infrastructure.client.binance;

import com.aicoinassist.batch.domain.market.dto.MarketDerivativeSnapshot;
import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceOpenInterestResponse;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinancePremiumIndexResponse;
import com.aicoinassist.batch.infrastructure.client.binance.validator.BinanceOpenInterestResponseValidator;
import com.aicoinassist.batch.infrastructure.client.binance.validator.BinancePremiumIndexResponseValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class BinanceDerivativesClient {

    private final BinanceApiClient binanceApiClient;
    private final BinanceOpenInterestResponseValidator openInterestResponseValidator;
    private final BinancePremiumIndexResponseValidator premiumIndexResponseValidator;
    private final ObjectMapper objectMapper;

    public MarketDerivativeSnapshot fetchSnapshot(String symbol) {
        BinanceOpenInterestResponse openInterestResponse = binanceApiClient.getOpenInterest(symbol);
        BinancePremiumIndexResponse premiumIndexResponse = binanceApiClient.getPremiumIndex(symbol);

        RawDataValidationResult openInterestValidation =
                openInterestResponseValidator.validate(symbol, openInterestResponse);
        RawDataValidationResult premiumIndexValidation =
                premiumIndexResponseValidator.validate(symbol, premiumIndexResponse);

        return new MarketDerivativeSnapshot(
                symbol,
                toInstant(openInterestResponse == null ? null : openInterestResponse.time()),
                openInterestValidation,
                parseDecimal(openInterestResponse == null ? null : openInterestResponse.openInterest()),
                serialize(openInterestResponse),
                toInstant(premiumIndexResponse == null ? null : premiumIndexResponse.time()),
                premiumIndexValidation,
                parseDecimal(premiumIndexResponse == null ? null : premiumIndexResponse.markPrice()),
                parseDecimal(premiumIndexResponse == null ? null : premiumIndexResponse.indexPrice()),
                parseDecimal(premiumIndexResponse == null ? null : premiumIndexResponse.lastFundingRate()),
                toInstant(premiumIndexResponse == null ? null : premiumIndexResponse.nextFundingTime()),
                serialize(premiumIndexResponse)
        );
    }

    private Instant toInstant(Long epochMilli) {
        return epochMilli == null ? null : Instant.ofEpochMilli(epochMilli);
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

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize Binance derivatives payload.", exception);
        }
    }
}
