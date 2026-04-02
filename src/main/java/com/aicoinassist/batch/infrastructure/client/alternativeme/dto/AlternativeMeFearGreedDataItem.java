package com.aicoinassist.batch.infrastructure.client.alternativeme.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AlternativeMeFearGreedDataItem(
        String value,
        @JsonProperty("value_classification")
        String valueClassification,
        String timestamp,
        @JsonProperty("time_until_update")
        String timeUntilUpdate
) {
}
