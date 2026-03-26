package com.aicoinassist.api.domain.report.dto;

import com.aicoinassist.api.domain.report.enumtype.ReportType;
import java.math.BigDecimal;
import java.time.Instant;

public record ReportHistoryItemResponse(
	Long reportId,
	Long narrativeId,
	String symbol,
	ReportType reportType,
	Instant analysisBasisTime,
	Instant rawReferenceTime,
	Instant priceSourceEventTime,
	Instant reportStoredTime,
	String headline,
	String outlook,
	String confidence,
	String oneLineTake,
	String marketRegime,
	BigDecimal currentPrice,
	BigDecimal dailyPriceChangeRate,
	String trendLabel,
	String volatilityLabel,
	boolean narrativeAvailable
) {
}
