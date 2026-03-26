package com.aicoinassist.api.domain.asset.controller;

import com.aicoinassist.api.domain.asset.dto.AssetSummaryCardResponse;
import com.aicoinassist.api.domain.asset.dto.AssetSummaryListResponse;
import com.aicoinassist.api.domain.asset.dto.SupportedAssetResponse;
import com.aicoinassist.api.domain.asset.service.AssetSummaryReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assets")
@Tag(name = "Assets", description = "Asset summary and supported asset APIs")
public class AssetQueryController {

	private final AssetSummaryReadService assetSummaryReadService;

	@GetMapping
	@Operation(summary = "Get supported assets", description = "Returns the supported asset symbols and display metadata.")
	public List<SupportedAssetResponse> getSupportedAssets() {
		return assetSummaryReadService.getSupportedAssets();
	}

	@GetMapping("/summaries")
	@Operation(summary = "Get asset summaries", description = "Returns dashboard-oriented asset summary cards.")
	public AssetSummaryListResponse getAssetSummaries() {
		return assetSummaryReadService.getAssetSummaries();
	}

	@GetMapping("/{symbol}/summary")
	@Operation(summary = "Get asset summary", description = "Returns the dashboard summary card for a single asset symbol.")
	public AssetSummaryCardResponse getAssetSummary(
		@PathVariable @Pattern(regexp = "^[A-Z0-9]{2,20}USDT$") String symbol
	) {
		return assetSummaryReadService.getAssetSummary(symbol);
	}
}
