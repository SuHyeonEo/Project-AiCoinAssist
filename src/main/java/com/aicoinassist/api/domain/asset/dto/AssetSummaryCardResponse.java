package com.aicoinassist.api.domain.asset.dto;

import com.aicoinassist.api.domain.report.dto.MarketParticipationSummaryResponse;
import com.aicoinassist.api.domain.report.enumtype.ReportType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AssetSummaryCardResponse(
	String symbol,
	String assetCode,
	String assetName,
	BigDecimal latestPrice,
	BigDecimal dailyPriceChangeRate,
	String participationWindowLabel,
	BigDecimal quoteVolumeChangeRate,
	BigDecimal tradeCountChangeRate,
	BigDecimal takerBuyQuoteRatio,
	BigDecimal takerBuyQuoteRatioDelta,
	String trendLabel,
	String volatilityLabel,
	String overallTone,
	String outlook,
	String headline,
	ReportType summaryReportType,
	Instant latestAnalysisBasisTime,
	Instant priceSourceEventTime,
	Instant latestReportStoredTime,
	MarketParticipationSummaryResponse marketParticipation,
	List<AssetReportStatusResponse> reportStatuses
) {
}
