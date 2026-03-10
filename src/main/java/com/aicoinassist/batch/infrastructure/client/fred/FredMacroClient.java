package com.aicoinassist.batch.infrastructure.client.fred;

import com.aicoinassist.batch.domain.macro.dto.FredMacroRawSnapshot;
import com.aicoinassist.batch.domain.macro.enumtype.MacroMetricType;
import com.aicoinassist.batch.domain.market.validator.RawDataValidationResult;
import com.aicoinassist.batch.global.config.FredProperties;
import com.aicoinassist.batch.infrastructure.client.fred.dto.FredObservationItem;
import com.aicoinassist.batch.infrastructure.client.fred.dto.FredObservationResponse;
import com.aicoinassist.batch.infrastructure.client.fred.validator.FredObservationResponseValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class FredMacroClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final FredProperties fredProperties;
    private final FredObservationResponseValidator validator;

    public FredMacroRawSnapshot fetchLatestObservation(MacroMetricType metricType) {
        String apiKey = fredProperties.apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("FRED API key must be configured.");
        }

        String seriesId = seriesId(metricType);
        String rawPayload = restClient.get()
                                      .uri(
                                              fredProperties.baseUrl()
                                                      + "/fred/series/observations?series_id="
                                                      + seriesId
                                                      + "&api_key="
                                                      + apiKey
                                                      + "&file_type=json&sort_order=desc&limit=10"
                                      )
                                      .retrieve()
                                      .body(String.class);

        if (rawPayload == null) {
            throw new IllegalStateException("FRED API returned an empty payload for " + metricType + ".");
        }

        FredObservationResponse response = deserialize(rawPayload);
        RawDataValidationResult validation = validator.validate(response);
        FredObservationItem item = latestUsableObservation(response);

        return new FredMacroRawSnapshot(
                metricType,
                seriesId,
                response.units(),
                parseDate(item == null ? null : item.date()),
                validation,
                parseDecimal(item == null ? null : item.value()),
                rawPayload
        );
    }

    private String seriesId(MacroMetricType metricType) {
        return switch (metricType) {
            case DXY_PROXY -> fredProperties.series().dxyProxy();
            case US10Y_YIELD -> fredProperties.series().us10yYield();
            case USD_KRW -> fredProperties.series().usdKrw();
        };
    }

    private FredObservationResponse deserialize(String rawPayload) {
        try {
            return objectMapper.readValue(rawPayload, FredObservationResponse.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to deserialize FRED observation response.", exception);
        }
    }

    private FredObservationItem latestUsableObservation(FredObservationResponse response) {
        if (response == null || response.observations() == null) {
            return null;
        }

        for (FredObservationItem item : response.observations()) {
            if (item == null) {
                continue;
            }
            if (parseDate(item.date()) == null) {
                continue;
            }
            if (parseDecimal(item.value()) == null) {
                continue;
            }
            return item;
        }
        return null;
    }

    private LocalDate parseDate(String rawValue) {
        try {
            return rawValue == null || rawValue.isBlank() ? null : LocalDate.parse(rawValue);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private BigDecimal parseDecimal(String rawValue) {
        try {
            if (rawValue == null || rawValue.isBlank() || ".".equals(rawValue.trim())) {
                return null;
            }
            return new BigDecimal(rawValue);
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
