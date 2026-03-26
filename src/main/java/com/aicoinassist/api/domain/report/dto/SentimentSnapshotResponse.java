package com.aicoinassist.api.domain.report.dto;

import java.math.BigDecimal;

public record SentimentSnapshotResponse(
	BigDecimal indexValue,
	String classification,
	BigDecimal valueChange,
	BigDecimal valueChangeRate
) {
}
