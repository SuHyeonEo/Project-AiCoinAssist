package com.aicoinassist.batch.infrastructure.client.coinmetrics;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.domain.onchain.dto.CoinMetricsOnchainRawSnapshot;
import com.aicoinassist.batch.domain.onchain.enumtype.OnchainMetricType;
import com.aicoinassist.batch.global.config.CoinMetricsProperties;
import com.aicoinassist.batch.infrastructure.client.coinmetrics.dto.CoinMetricsAssetMetricsResponse;
import com.aicoinassist.batch.infrastructure.client.coinmetrics.validator.CoinMetricsAssetMetricsResponseValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CoinMetricsOnchainClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final CoinMetricsProperties coinMetricsProperties;
    private final CoinMetricsAssetMetricsResponseValidator validator;

    public CoinMetricsOnchainRawSnapshot fetchLatestMetric(String assetCode, OnchainMetricType metricType) {
        String rawPayload = restClient.get()
                                      .uri(
                                              coinMetricsProperties.baseUrl()
                                                      + "/timeseries/asset-metrics?assets="
                                                      + assetCode
                                                      + "&metrics="
                                                      + metricType.coinMetricsMetricId()
                                                      + "&frequency=1d&limit_per_asset=1&paging_from=end"
                                      )
                                      .retrieve()
                                      .body(String.class);

        if (rawPayload == null) {
            throw new IllegalStateException("Coin Metrics API returned an empty payload for " + assetCode + " " + metricType + ".");
        }

        CoinMetricsAssetMetricsResponse response = deserialize(rawPayload);
        RawDataValidationResult validation = validator.validate(response, metricType);
        Map<String, String> item = firstItem(response);

        return new CoinMetricsOnchainRawSnapshot(
                assetCode,
                metricType,
                parseInstant(item == null ? null : item.get("time")),
                validation,
                parseDecimal(item == null ? null : item.get(metricType.coinMetricsMetricId())),
                rawPayload
        );
    }

    private CoinMetricsAssetMetricsResponse deserialize(String rawPayload) {
        try {
            return objectMapper.readValue(rawPayload, CoinMetricsAssetMetricsResponse.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize Coin Metrics API response.", exception);
        }
    }

    private Map<String, String> firstItem(CoinMetricsAssetMetricsResponse response) {
        if (response == null || response.data() == null || response.data().isEmpty()) {
            return null;
        }
        return response.data().get(0);
    }

    private Instant parseInstant(String rawValue) {
        try {
            return rawValue == null || rawValue.isBlank() ? null : Instant.parse(rawValue);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private BigDecimal parseDecimal(String rawValue) {
        try {
            return rawValue == null || rawValue.isBlank() ? null : new BigDecimal(rawValue);
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
