package com.aicoinassist.batch.infrastructure.client.binance.validator;

import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceTickerPriceResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BinanceTickerPriceResponseValidatorTest {

    private final BinanceTickerPriceResponseValidator validator = new BinanceTickerPriceResponseValidator();

    @Test
    void validateReturnsValidForExpectedTickerPayload() {
        BinanceTickerPriceResponse response = new BinanceTickerPriceResponse("BTCUSDT", "87500.12000000");

        RawDataValidationResult result = validator.validate("BTCUSDT", response);

        assertThat(result.status()).isEqualTo(RawDataValidationStatus.VALID);
        assertThat(result.details()).isNull();
    }

    @Test
    void validateReturnsInvalidWhenTickerSymbolDoesNotMatch() {
        BinanceTickerPriceResponse response = new BinanceTickerPriceResponse("ETHUSDT", "2200.50000000");

        RawDataValidationResult result = validator.validate("BTCUSDT", response);

        assertThat(result.status()).isEqualTo(RawDataValidationStatus.INVALID);
        assertThat(result.details()).contains("does not match");
    }

    @Test
    void validateReturnsInvalidWhenTickerPriceIsNegative() {
        BinanceTickerPriceResponse response = new BinanceTickerPriceResponse("BTCUSDT", "-1.0");

        RawDataValidationResult result = validator.validate("BTCUSDT", response);

        assertThat(result.status()).isEqualTo(RawDataValidationStatus.INVALID);
        assertThat(result.details()).contains("non-negative");
    }
}
