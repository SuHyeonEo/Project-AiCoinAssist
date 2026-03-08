package com.aicoinassist.batch.infrastructure.client.binance.validator;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceKlineResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class BinanceKlineResponseValidator {

    public RawDataValidationResult validateSequence(List<BinanceKlineResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            return RawDataValidationResult.invalid("Kline response list is empty.");
        }

        Set<Long> openTimes = new HashSet<>();
        Long previousOpenTime = null;

        for (BinanceKlineResponse response : responses) {
            if (response == null || response.openTime() == null) {
                return RawDataValidationResult.invalid("Kline open time is missing.");
            }

            if (!openTimes.add(response.openTime())) {
                return RawDataValidationResult.invalid("Kline response list contains duplicate open times.");
            }

            if (previousOpenTime != null && response.openTime() <= previousOpenTime) {
                return RawDataValidationResult.invalid("Kline response list is not in strictly increasing open time order.");
            }

            previousOpenTime = response.openTime();
        }

        return RawDataValidationResult.valid();
    }

    public RawDataValidationResult validateItem(BinanceKlineResponse response) {
        if (response == null) {
            return RawDataValidationResult.invalid("Kline response item is null.");
        }

        if (response.openTime() == null || response.closeTime() == null) {
            return RawDataValidationResult.invalid("Kline timestamp is missing.");
        }

        if (response.closeTime() <= response.openTime()) {
            return RawDataValidationResult.invalid("Kline close time must be later than open time.");
        }

        BigDecimal open = parseDecimal(response.open());
        BigDecimal high = parseDecimal(response.high());
        BigDecimal low = parseDecimal(response.low());
        BigDecimal close = parseDecimal(response.close());
        BigDecimal volume = parseDecimal(response.volume());

        if (open == null || high == null || low == null || close == null || volume == null) {
            return RawDataValidationResult.invalid("Kline OHLCV contains an invalid decimal value.");
        }

        if (volume.compareTo(BigDecimal.ZERO) < 0) {
            return RawDataValidationResult.invalid("Kline volume must be non-negative.");
        }

        BigDecimal maxPrice = open.max(high).max(low).max(close);
        BigDecimal minPrice = open.min(high).min(low).min(close);

        if (high.compareTo(maxPrice) < 0) {
            return RawDataValidationResult.invalid("Kline high must be greater than or equal to open, low, and close.");
        }

        if (low.compareTo(minPrice) > 0) {
            return RawDataValidationResult.invalid("Kline low must be less than or equal to open, high, and close.");
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
