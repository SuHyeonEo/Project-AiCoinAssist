package com.aicoinassist.api.domain.report.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record MarketParticipationSummaryResponse(
	String title,
	String summary,
	String representativeWindowLabel,
	Instant currentWindowStartTime,
	Instant currentWindowEndTime,
	Instant previousWindowStartTime,
	Instant previousWindowEndTime,
	Integer sampleCount,
	BigDecimal priceChangeRate,
	BigDecimal quoteVolumeChangeRate,
	BigDecimal tradeCountChangeRate,
	BigDecimal takerBuyQuoteRatio,
	BigDecimal takerBuyQuoteRatioDelta,
	Instant analysisBasisTime,
	Instant priceSourceEventTime,
	Instant openTime,
	Instant closeTime,
	List<String> highlights,
	List<String> facts,
	List<ParticipationWindowMetricResponse> summaries
) {
}
