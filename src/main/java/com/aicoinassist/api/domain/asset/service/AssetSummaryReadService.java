package com.aicoinassist.api.domain.asset.service;

import com.aicoinassist.api.domain.asset.dto.AssetReportStatusResponse;
import com.aicoinassist.api.domain.asset.dto.AssetSummaryCardResponse;
import com.aicoinassist.api.domain.asset.dto.AssetSummaryListResponse;
import com.aicoinassist.api.domain.asset.dto.SupportedAssetResponse;
import com.aicoinassist.api.domain.asset.support.AssetDisplayNameSupport;
import com.aicoinassist.api.domain.report.dto.ReportDetailResponse;
import com.aicoinassist.api.domain.report.dto.ReportSummaryResponse;
import com.aicoinassist.api.domain.report.enumtype.ReportType;
import com.aicoinassist.api.domain.report.exception.ReportNotFoundException;
import com.aicoinassist.api.domain.report.service.ReportReadService;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchProperties;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssetSummaryReadService {

	private final AnalysisReportRepository analysisReportRepository;
	private final ReportReadService reportReadService;
	private final AssetDisplayNameSupport assetDisplayNameSupport;
	private final AnalysisReportBatchProperties analysisReportBatchProperties;

	public List<SupportedAssetResponse> getSupportedAssets() {
		return configuredSymbols().stream()
			.sorted(assetDisplayNameSupport.symbolComparator())
			.map(symbol -> new SupportedAssetResponse(
				symbol,
				assetDisplayNameSupport.assetCode(symbol),
				assetDisplayNameSupport.assetName(symbol)
			))
			.toList();
	}

	public AssetSummaryListResponse getAssetSummaries() {
		List<AssetSummaryCardResponse> items = configuredSymbols().stream()
			.sorted(assetDisplayNameSupport.symbolComparator())
			.map(this::getAssetSummary)
			.toList();
		return new AssetSummaryListResponse(items);
	}

	public AssetSummaryCardResponse getAssetSummary(String symbol) {
		if (!configuredSymbols().contains(symbol)) {
			throw new ReportNotFoundException("Asset summary not found: " + symbol);
		}
		analysisReportRepository.findTopBySymbolOrderByAnalysisBasisTimeDescIdDesc(symbol)
			.orElseThrow(() -> new ReportNotFoundException("Asset summary not found: " + symbol));
		AnalysisReportEntity summaryReport = getDashboardSummaryReport(symbol);
		ReportSummaryResponse latestSummary = reportReadService.getLatestSummary(
			symbol,
			toApiReportType(summaryReport.getReportType())
		);

		List<AssetReportStatusResponse> reportStatuses = new ArrayList<>();
		for (ReportType reportType : ReportType.values()) {
			analysisReportRepository.findTopBySymbolAndReportTypeOrderByAnalysisBasisTimeDescIdDesc(
				symbol,
				toBatchReportType(reportType)
			)
				.ifPresentOrElse(
					report -> reportStatuses.add(toReportStatus(report)),
					() -> reportStatuses.add(unavailableReportStatus(reportType))
				);
		}

		return new AssetSummaryCardResponse(
			symbol,
			assetDisplayNameSupport.assetCode(symbol),
			assetDisplayNameSupport.assetName(symbol),
			latestSummary.snapshot().currentPrice(),
			latestSummary.snapshot().dailyPriceChangeRate(),
			latestSummary.snapshot().participationWindowLabel(),
			latestSummary.snapshot().quoteVolumeChangeRate(),
			latestSummary.snapshot().tradeCountChangeRate(),
			latestSummary.snapshot().takerBuyQuoteRatio(),
			latestSummary.snapshot().takerBuyQuoteRatioDelta(),
			latestSummary.snapshot().trendLabel(),
			latestSummary.snapshot().volatilityLabel(),
			latestSummary.hero().marketRegime(),
			latestSummary.header().outlook(),
			latestSummary.header().headline(),
			toApiReportType(summaryReport.getReportType()),
			latestSummary.meta().analysisBasisTime(),
			latestSummary.meta().priceSourceEventTime(),
			latestSummary.meta().reportStoredTime(),
			latestSummary.marketParticipation(),
			reportStatuses
		);
	}

	private AnalysisReportEntity getDashboardSummaryReport(String symbol) {
		for (AnalysisReportType reportType : dashboardReportTypeOrder()) {
			Optional<AnalysisReportEntity> report = analysisReportRepository
				.findTopBySymbolAndReportTypeOrderByAnalysisBasisTimeDescIdDesc(symbol, reportType);
			if (report.isPresent()) {
				return report.get();
			}
		}
		throw new ReportNotFoundException("Asset summary not found: " + symbol);
	}

	private List<AnalysisReportType> dashboardReportTypeOrder() {
		return List.of(AnalysisReportType.LONG_TERM, AnalysisReportType.MID_TERM, AnalysisReportType.SHORT_TERM);
	}

	private AssetReportStatusResponse toReportStatus(AnalysisReportEntity report) {
		ReportDetailResponse detail = reportReadService.getDetail(report.getId());

		return new AssetReportStatusResponse(
			toApiReportType(report.getReportType()),
			true,
			report.getId(),
			report.getAnalysisBasisTime(),
			report.getStoredTime(),
			detail.header().headline(),
			detail.header().outlook(),
			detail.header().overallTone(),
			detail.meta().narrativeAvailable()
		);
	}

	private AssetReportStatusResponse unavailableReportStatus(ReportType reportType) {
		return new AssetReportStatusResponse(
			reportType,
			false,
			null,
			null,
			null,
			null,
			null,
			null,
			false
		);
	}

	private ReportType toApiReportType(AnalysisReportType reportType) {
		return ReportType.valueOf(reportType.name());
	}

	private AnalysisReportType toBatchReportType(ReportType reportType) {
		return AnalysisReportType.valueOf(reportType.name());
	}

	private List<String> configuredSymbols() {
		return analysisReportBatchProperties.assetTypes().stream()
			.map(assetType -> assetType.symbol())
			.toList();
	}
}
