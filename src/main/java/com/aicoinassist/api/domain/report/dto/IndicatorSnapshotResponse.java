package com.aicoinassist.api.domain.report.dto;

import java.math.BigDecimal;

public record IndicatorSnapshotResponse(
	BigDecimal value,
	BigDecimal delta,
	String signalSummary
) {
}
