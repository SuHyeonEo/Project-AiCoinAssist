package com.aicoinassist.api.domain.report.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PriceLevelResponse(
	String label,
	String sourceType,
	BigDecimal price,
	BigDecimal distanceFromCurrent,
	BigDecimal strengthScore,
	Instant referenceTime,
	Integer reactionCount,
	Integer clusterSize,
	String rationale,
	List<String> triggerFacts
) {
}
