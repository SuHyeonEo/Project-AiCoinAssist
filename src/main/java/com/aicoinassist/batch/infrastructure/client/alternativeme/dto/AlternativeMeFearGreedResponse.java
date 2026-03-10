package com.aicoinassist.batch.infrastructure.client.alternativeme.dto;

import java.util.List;

public record AlternativeMeFearGreedResponse(
        String name,
        List<AlternativeMeFearGreedDataItem> data,
        AlternativeMeFearGreedMetadata metadata
) {
}
