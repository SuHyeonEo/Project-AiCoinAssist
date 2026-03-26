package com.aicoinassist.api.domain.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record PriceZoneResponse(
	String zoneType,
	Integer zoneRank,
	BigDecimal representativePrice,
	BigDecimal zoneLow,
	BigDecimal zoneHigh,
	BigDecimal distanceFromCurrent,
	BigDecimal distanceToZone,
	BigDecimal strengthScore,
	String interactionType,
	String strongestLevelLabel,
	String strongestSourceType,
	Integer levelCount,
	Integer recentTestCount,
	Integer recentRejectionCount,
	Integer recentBreakCount,
	List<String> triggerFacts
) {
}
