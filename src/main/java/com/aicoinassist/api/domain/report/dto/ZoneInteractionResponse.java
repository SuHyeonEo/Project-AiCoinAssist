package com.aicoinassist.api.domain.report.dto;

import java.util.List;

public record ZoneInteractionResponse(
	String zoneType,
	Integer zoneRank,
	String interactionType,
	String summary,
	List<String> triggerFacts
) {
}
