package com.aicoinassist.batch.infrastructure.client.coinmetrics.dto;

import java.util.List;
import java.util.Map;

public record CoinMetricsAssetMetricsResponse(
        List<Map<String, String>> data,
        String nextPageToken,
        String nextPageUrl
) {
}
