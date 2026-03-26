package com.aicoinassist.api.domain.report.dto;

import java.util.List;

public record CrossSignalIntegrationResponse(
	String alignmentSummary,
	List<String> dominantDrivers,
	String conflictSummary,
	String positioningTake
) {
}
