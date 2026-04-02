package com.aicoinassist.batch.infrastructure.client.binance.validator;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinancePremiumIndexResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BinancePremiumIndexResponseValidator {

    public RawDataValidationResult validate(String expectedSymbol, BinancePremiumIndexResponse response) {
        if (response == null) {
            return RawDataValidationResult.invalid("Premium index response must not be null.");
        }

        if (response.symbol() == null || response.symbol().isBlank()) {
            return RawDataValidationResult.invalid("Premium index symbol must not be blank.");
        }

        if (expectedSymbol != null && !expectedSymbol.equals(response.symbol())) {
            return RawDataValidationResult.invalid("Premium index symbol must match the requested symbol.");
        }

        if (response.time() == null || response.time() < 0) {
            return RawDataValidationResult.invalid("Premium index time must be present and non-negative.");
        }

        if (!isNonNegativeDecimal(response.markPrice())) {
            return RawDataValidationResult.invalid("Premium index mark price must be numeric and non-negative.");
        }

        if (!isNonNegativeDecimal(response.indexPrice())) {
            return RawDataValidationResult.invalid("Premium index index price must be numeric and non-negative.");
        }

        if (!isDecimal(response.lastFundingRate())) {
            return RawDataValidationResult.invalid("Premium index last funding rate must be numeric.");
        }

        if (response.nextFundingTime() == null || response.nextFundingTime() < 0) {
            return RawDataValidationResult.invalid("Premium index next funding time must be present and non-negative.");
        }

        return RawDataValidationResult.valid();
    }

    private boolean isNonNegativeDecimal(String value) {
        if (!isDecimal(value)) {
            return false;
        }

        return new BigDecimal(value).compareTo(BigDecimal.ZERO) >= 0;
    }

    private boolean isDecimal(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        try {
            new BigDecimal(value);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
