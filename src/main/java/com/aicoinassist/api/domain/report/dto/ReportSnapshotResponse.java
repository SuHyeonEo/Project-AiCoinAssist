package com.aicoinassist.api.domain.report.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ReportSnapshotResponse(
	BigDecimal currentPrice,
	BigDecimal dailyPriceChangeRate,
	Instant priceSourceEventTime,
	String participationWindowLabel,
	BigDecimal quoteVolumeChangeRate,
	BigDecimal tradeCountChangeRate,
	BigDecimal takerBuyQuoteRatio,
	BigDecimal takerBuyQuoteRatioDelta,
	RangeSnapshotResponse range7d,
	IndicatorSnapshotResponse rsi14,
	IndicatorSnapshotResponse macdHistogram,
	SentimentSnapshotResponse fearGreed,
	MacroSnapshotResponse macro,
	String trendLabel,
	String volatilityLabel,
	String rangePositionLabel
) {
}
