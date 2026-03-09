package com.aicoinassist.batch.infrastructure.client.binance.validator;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceOpenInterestResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BinanceOpenInterestResponseValidator {

    public RawDataValidationResult validate(String expectedSymbol, BinanceOpenInterestResponse response) {
        if (response == null) {
            return RawDataValidationResult.invalid("Open interest response must not be null.");
        }

        if (response.symbol() == null || response.symbol().isBlank()) {
            return RawDataValidationResult.invalid("Open interest symbol must not be blank.");
        }

        if (expectedSymbol != null && !expectedSymbol.equals(response.symbol())) {
            return RawDataValidationResult.invalid("Open interest symbol must match the requested symbol.");
        }

        if (response.time() == null || response.time() < 0) {
            return RawDataValidationResult.invalid("Open interest time must be present and non-negative.");
        }

        if (response.openInterest() == null || response.openInterest().isBlank()) {
            return RawDataValidationResult.invalid("Open interest value must not be blank.");
        }

        try {
            if (new BigDecimal(response.openInterest()).compareTo(BigDecimal.ZERO) < 0) {
                return RawDataValidationResult.invalid("Open interest value must be greater than or equal to zero.");
            }
        } catch (NumberFormatException exception) {
            return RawDataValidationResult.invalid("Open interest value must be numeric.");
        }

        return RawDataValidationResult.valid();
    }
}
