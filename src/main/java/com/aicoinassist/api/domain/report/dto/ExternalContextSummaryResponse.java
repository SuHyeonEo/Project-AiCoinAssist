package com.aicoinassist.api.domain.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record ExternalContextSummaryResponse(
	BigDecimal compositeRiskScore,
	String dominantDirection,
	String highestSeverity,
	Integer supportiveSignalCount,
	Integer cautionarySignalCount,
	Integer headwindSignalCount,
	String primarySignalCategory,
	String primarySignalTitle,
	String primarySignalDetail,
	List<ExternalRegimeSignalResponse> regimeSignals
) {
}
