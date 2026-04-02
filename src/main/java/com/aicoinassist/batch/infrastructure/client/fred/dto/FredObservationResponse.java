package com.aicoinassist.batch.infrastructure.client.fred.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FredObservationResponse(
        String units,
        @JsonProperty("error_code")
        Integer errorCode,
        @JsonProperty("error_message")
        String errorMessage,
        List<FredObservationItem> observations
) {
}
