package com.aicoinassist.batch.infrastructure.client.binance.validator;

import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceKlineResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BinanceKlineResponseValidatorTest {

    private final BinanceKlineResponseValidator validator = new BinanceKlineResponseValidator();

    @Test
    void validateSequenceReturnsValidForAscendingOpenTimes() {
        List<BinanceKlineResponse> responses = List.of(
                kline(1000L, 1999L, "10", "12", "9", "11", "100"),
                kline(2000L, 2999L, "11", "13", "10", "12", "120")
        );

        RawDataValidationResult result = validator.validateSequence(responses);

        assertThat(result.status()).isEqualTo(RawDataValidationStatus.VALID);
    }

    @Test
    void validateSequenceReturnsInvalidForDescendingOpenTimes() {
        List<BinanceKlineResponse> responses = List.of(
                kline(2000L, 2999L, "11", "13", "10", "12", "120"),
                kline(1000L, 1999L, "10", "12", "9", "11", "100")
        );

        RawDataValidationResult result = validator.validateSequence(responses);

        assertThat(result.status()).isEqualTo(RawDataValidationStatus.INVALID);
        assertThat(result.details()).contains("strictly increasing");
    }

    @Test
    void validateItemReturnsInvalidForBrokenHighLowRelationship() {
        BinanceKlineResponse response = kline(1000L, 1999L, "10", "11", "12", "10.5", "100");

        RawDataValidationResult result = validator.validateItem(response);

        assertThat(result.status()).isEqualTo(RawDataValidationStatus.INVALID);
        assertThat(result.details()).contains("low must be less than or equal");
    }

    private BinanceKlineResponse kline(
            Long openTime,
            Long closeTime,
            String open,
            String high,
            String low,
            String close,
            String volume
    ) {
        return new BinanceKlineResponse(
                openTime,
                open,
                high,
                low,
                close,
                volume,
                closeTime,
                "0",
                10L,
                "0",
                "0",
                "0",
                List.of(
                        String.valueOf(openTime),
                        open,
                        high,
                        low,
                        close,
                        volume,
                        String.valueOf(closeTime),
                        "0",
                        "10",
                        "0",
                        "0",
                        "0"
                )
        );
    }
}
