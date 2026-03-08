package com.aicoinassist.batch.infrastructure.client.binance.validator;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceTickerPriceResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BinanceTickerPriceResponseValidator {

    public RawDataValidationResult validate(String requestedSymbol, BinanceTickerPriceResponse response) {
        if (response == null) {
            return RawDataValidationResult.invalid("Ticker price response is null.");
        }

        if (response.symbol() == null || response.symbol().isBlank()) {
            return RawDataValidationResult.invalid("Ticker price symbol is missing.");
        }

        if (!requestedSymbol.equals(response.symbol())) {
            return RawDataValidationResult.invalid("Ticker price symbol does not match requested symbol.");
        }

        BigDecimal price = parseDecimal(response.price());
        if (price == null) {
            return RawDataValidationResult.invalid("Ticker price is not a valid decimal value.");
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            return RawDataValidationResult.invalid("Ticker price must be non-negative.");
        }

        return RawDataValidationResult.valid();
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
}
