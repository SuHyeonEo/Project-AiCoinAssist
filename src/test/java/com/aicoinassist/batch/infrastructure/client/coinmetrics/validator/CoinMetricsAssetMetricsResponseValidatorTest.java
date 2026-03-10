package com.aicoinassist.batch.infrastructure.client.coinmetrics.validator;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.domain.onchain.enumtype.OnchainMetricType;
import com.aicoinassist.batch.infrastructure.client.coinmetrics.dto.CoinMetricsAssetMetricsResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CoinMetricsAssetMetricsResponseValidatorTest {

    private final CoinMetricsAssetMetricsResponseValidator validator = new CoinMetricsAssetMetricsResponseValidator();

    @Test
    void validateReturnsValidWhenResponseContainsUsableMetric() {
        CoinMetricsAssetMetricsResponse response = new CoinMetricsAssetMetricsResponse(
                List.of(Map.of(
                        "asset", "btc",
                        "time", "2026-03-09T00:00:00.000000000Z",
                        "AdrActCnt", "815234.00000000"
                )),
                null,
                null
        );

        RawDataValidationResult result = validator.validate(response, OnchainMetricType.ACTIVE_ADDRESS_COUNT);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void validateReturnsInvalidWhenMetricValueIsMissing() {
        CoinMetricsAssetMetricsResponse response = new CoinMetricsAssetMetricsResponse(
                List.of(Map.of(
                        "asset", "btc",
                        "time", "2026-03-09T00:00:00.000000000Z"
                )),
                null,
                null
        );

        RawDataValidationResult result = validator.validate(response, OnchainMetricType.ACTIVE_ADDRESS_COUNT);

        assertThat(result.isValid()).isFalse();
        assertThat(result.details()).contains("metric value");
    }

    @Test
    void validateReturnsInvalidWhenTimeIsMalformed() {
        CoinMetricsAssetMetricsResponse response = new CoinMetricsAssetMetricsResponse(
                List.of(Map.of(
                        "asset", "btc",
                        "time", "not-an-instant",
                        "TxCnt", "412345.00000000"
                )),
                null,
                null
        );

        RawDataValidationResult result = validator.validate(response, OnchainMetricType.TRANSACTION_COUNT);

        assertThat(result.isValid()).isFalse();
        assertThat(result.details()).contains("ISO-8601");
    }
}
