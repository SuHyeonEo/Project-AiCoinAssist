package com.aicoinassist.api.domain.report.dto;

import java.util.List;

public record LevelSummaryResponse(
	List<PriceLevelResponse> supportLevels,
	List<PriceLevelResponse> resistanceLevels,
	List<PriceZoneResponse> supportZones,
	List<PriceZoneResponse> resistanceZones,
	PriceZoneResponse nearestSupportZone,
	PriceZoneResponse nearestResistanceZone,
	List<ZoneInteractionResponse> zoneInteractionFacts
) {
}
