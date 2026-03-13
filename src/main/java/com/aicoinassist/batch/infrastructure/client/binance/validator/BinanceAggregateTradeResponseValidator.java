package com.aicoinassist.batch.infrastructure.client.binance.validator;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceAggregateTradeResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BinanceAggregateTradeResponseValidator {

    public RawDataValidationResult validate(BinanceAggregateTradeResponse response) {
        if (response == null) {
            return RawDataValidationResult.invalid("Aggregate trade response is null.");
        }
        if (response.tradeTime() == null) {
            return RawDataValidationResult.invalid("Aggregate trade time is missing.");
        }

        BigDecimal price = parseDecimal(response.price());
        if (price == null) {
            return RawDataValidationResult.invalid("Aggregate trade price is not a valid decimal value.");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            return RawDataValidationResult.invalid("Aggregate trade price must be non-negative.");
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
