package com.aicoinassist.api.domain.report.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RangeSnapshotResponse(
	String windowType,
	Instant windowStartTime,
	Instant windowEndTime,
	Instant openTime,
	Instant closeTime,
	BigDecimal high,
	BigDecimal low,
	BigDecimal range,
	BigDecimal currentPositionInRange,
	BigDecimal distanceFromWindowHigh,
	BigDecimal reboundFromWindowLow
) {
}
