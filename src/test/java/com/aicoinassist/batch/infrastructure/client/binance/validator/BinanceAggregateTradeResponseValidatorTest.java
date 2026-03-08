package com.aicoinassist.batch.infrastructure.client.binance.validator;

import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceAggregateTradeResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BinanceAggregateTradeResponseValidatorTest {

    private final BinanceAggregateTradeResponseValidator validator = new BinanceAggregateTradeResponseValidator();

    @Test
    void validateReturnsValidForExpectedAggregateTradePayload() {
        BinanceAggregateTradeResponse response = aggregateTrade(1L, "87500.12000000", "0.15000000", 1000L);

        RawDataValidationResult result = validator.validate(response);

        assertThat(result.status()).isEqualTo(RawDataValidationStatus.VALID);
        assertThat(result.details()).isNull();
    }

    @Test
    void validateReturnsInvalidWhenTradeTimeIsMissing() {
        BinanceAggregateTradeResponse response = aggregateTrade(1L, "87500.12000000", "0.15000000", null);

        RawDataValidationResult result = validator.validate(response);

        assertThat(result.status()).isEqualTo(RawDataValidationStatus.INVALID);
        assertThat(result.details()).contains("time is missing");
    }

    @Test
    void validateReturnsInvalidWhenPriceIsNegative() {
        BinanceAggregateTradeResponse response = aggregateTrade(1L, "-1.0", "0.15000000", 1000L);

        RawDataValidationResult result = validator.validate(response);

        assertThat(result.status()).isEqualTo(RawDataValidationStatus.INVALID);
        assertThat(result.details()).contains("price must be non-negative");
    }

    private BinanceAggregateTradeResponse aggregateTrade(
            Long aggregateTradeId,
            String price,
            String quantity,
            Long tradeTime
    ) {
        return new BinanceAggregateTradeResponse(
                aggregateTradeId,
                price,
                quantity,
                1L,
                1L,
                tradeTime,
                false,
                true
        );
    }
}
