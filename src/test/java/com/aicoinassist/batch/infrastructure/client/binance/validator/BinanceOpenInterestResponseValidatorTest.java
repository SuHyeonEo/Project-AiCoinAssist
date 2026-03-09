package com.aicoinassist.batch.infrastructure.client.binance.validator;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceOpenInterestResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BinanceOpenInterestResponseValidatorTest {

    private final BinanceOpenInterestResponseValidator validator = new BinanceOpenInterestResponseValidator();

    @Test
    void validateReturnsValidForWellFormedResponse() {
        RawDataValidationResult result = validator.validate(
                "BTCUSDT",
                new BinanceOpenInterestResponse("12345.678", "BTCUSDT", 1741564800000L)
        );

        assertThat(result.isValid()).isTrue();
        assertThat(result.details()).isNull();
    }

    @Test
    void validateReturnsInvalidWhenSymbolDoesNotMatch() {
        RawDataValidationResult result = validator.validate(
                "BTCUSDT",
                new BinanceOpenInterestResponse("12345.678", "ETHUSDT", 1741564800000L)
        );

        assertThat(result.isValid()).isFalse();
        assertThat(result.details()).contains("must match the requested symbol");
    }

    @Test
    void validateReturnsInvalidWhenOpenInterestIsNegative() {
        RawDataValidationResult result = validator.validate(
                "BTCUSDT",
                new BinanceOpenInterestResponse("-1", "BTCUSDT", 1741564800000L)
        );

        assertThat(result.isValid()).isFalse();
        assertThat(result.details()).contains("greater than or equal to zero");
    }
}
