package com.aicoinassist.api.domain.asset.dto;

public record SupportedAssetResponse(
	String symbol,
	String assetCode,
	String assetName
) {
}
