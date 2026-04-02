package com.aicoinassist.batch.infrastructure.client.alternativeme;

import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.domain.sentiment.dto.FearGreedRawSnapshot;
import com.aicoinassist.batch.domain.sentiment.enumtype.SentimentMetricType;
import com.aicoinassist.batch.global.config.AlternativeMeProperties;
import com.aicoinassist.batch.infrastructure.client.alternativeme.dto.AlternativeMeFearGreedDataItem;
import com.aicoinassist.batch.infrastructure.client.alternativeme.dto.AlternativeMeFearGreedResponse;
import com.aicoinassist.batch.infrastructure.client.alternativeme.validator.AlternativeMeFearGreedResponseValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class AlternativeMeSentimentClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final AlternativeMeProperties alternativeMeProperties;
    private final AlternativeMeFearGreedResponseValidator validator;

    public FearGreedRawSnapshot fetchLatestFearGreed() {
        String rawPayload = restClient.get()
                                      .uri(alternativeMeProperties.getBaseUrl() + "/fng/?limit=1")
                                      .retrieve()
                                      .body(String.class);

        if (rawPayload == null) {
            throw new IllegalStateException("Fear & Greed API returned an empty payload.");
        }

        AlternativeMeFearGreedResponse response = deserialize(rawPayload);
        RawDataValidationResult validation = validator.validate(response);
        AlternativeMeFearGreedDataItem item = firstItem(response);

        return new FearGreedRawSnapshot(
                SentimentMetricType.FEAR_GREED_INDEX,
                parseEpochSeconds(item == null ? null : item.timestamp()),
                validation,
                parseDecimal(item == null ? null : item.value()),
                item == null ? null : item.valueClassification(),
                parseLong(item == null ? null : item.timeUntilUpdate()),
                rawPayload
        );
    }

    private AlternativeMeFearGreedResponse deserialize(String rawPayload) {
        try {
            return objectMapper.readValue(rawPayload, AlternativeMeFearGreedResponse.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize Fear & Greed API response.", exception);
        }
    }

    private AlternativeMeFearGreedDataItem firstItem(AlternativeMeFearGreedResponse response) {
        if (response == null || response.data() == null || response.data().isEmpty()) {
            return null;
        }
        return response.data().get(0);
    }

    private Instant parseEpochSeconds(String rawValue) {
        try {
            return rawValue == null || rawValue.isBlank() ? null : Instant.ofEpochSecond(Long.parseLong(rawValue));
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

    private Long parseLong(String rawValue) {
        try {
            return rawValue == null || rawValue.isBlank() ? null : Long.parseLong(rawValue);
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
