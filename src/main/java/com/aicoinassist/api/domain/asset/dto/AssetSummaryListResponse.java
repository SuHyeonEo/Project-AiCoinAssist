package com.aicoinassist.api.domain.asset.dto;

import java.util.List;

public record AssetSummaryListResponse(
	List<AssetSummaryCardResponse> items
) {
}
