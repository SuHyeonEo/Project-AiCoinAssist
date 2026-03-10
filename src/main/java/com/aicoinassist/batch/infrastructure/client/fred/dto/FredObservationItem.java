package com.aicoinassist.batch.infrastructure.client.fred.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FredObservationItem(
        @JsonProperty("realtime_start")
        String realtimeStart,
        @JsonProperty("realtime_end")
        String realtimeEnd,
        String date,
        String value
) {
}
