package com.aicoinassist.batch.infrastructure.client.coinmetrics.validator;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.domain.onchain.enumtype.OnchainMetricType;
import com.aicoinassist.batch.infrastructure.client.coinmetrics.dto.CoinMetricsAssetMetricsResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Component
public class CoinMetricsAssetMetricsResponseValidator {

    public RawDataValidationResult validate(CoinMetricsAssetMetricsResponse response, OnchainMetricType metricType) {
        if (response == null) {
            return RawDataValidationResult.invalid("Coin Metrics response must not be null.");
        }

        if (response.data() == null || response.data().isEmpty()) {
            return RawDataValidationResult.invalid("Coin Metrics response must contain at least one data item.");
        }

        Map<String, String> item = response.data().get(0);
        if (item == null) {
            return RawDataValidationResult.invalid("Coin Metrics response item must not be null.");
        }

        String asset = item.get("asset");
        if (asset == null || asset.isBlank()) {
            return RawDataValidationResult.invalid("Coin Metrics asset must not be blank.");
        }

        try {
            String time = item.get("time");
            if (time == null || time.isBlank()) {
                return RawDataValidationResult.invalid("Coin Metrics time must not be blank.");
            }
            Instant.parse(time);
        } catch (RuntimeException exception) {
            return RawDataValidationResult.invalid("Coin Metrics time must be ISO-8601 instant.");
        }

        try {
            String metricValue = item.get(metricType.coinMetricsMetricId());
            if (metricValue == null || metricValue.isBlank()) {
                return RawDataValidationResult.invalid("Coin Metrics metric value must not be blank.");
            }
            BigDecimal parsed = new BigDecimal(metricValue);
            if (parsed.compareTo(BigDecimal.ZERO) < 0) {
                return RawDataValidationResult.invalid("Coin Metrics metric value must be greater than or equal to zero.");
            }
        } catch (RuntimeException exception) {
            return RawDataValidationResult.invalid("Coin Metrics metric value must be numeric.");
        }

        return RawDataValidationResult.valid();
    }
}
