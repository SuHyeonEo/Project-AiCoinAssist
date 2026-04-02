package com.aicoinassist.api.domain.report.dto;

import java.math.BigDecimal;

public record MacroSnapshotResponse(
	BigDecimal dxyProxyValue,
	BigDecimal dxyChangeRate,
	BigDecimal us10yYieldValue,
	BigDecimal us10yYieldChangeRate,
	BigDecimal usdKrwValue,
	BigDecimal usdKrwChangeRate
) {
}
