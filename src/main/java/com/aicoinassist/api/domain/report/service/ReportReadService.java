package com.aicoinassist.api.domain.report.service;

import com.aicoinassist.api.domain.report.dto.CrossSignalIntegrationResponse;
import com.aicoinassist.api.domain.report.dto.DomainAnalysisResponse;
import com.aicoinassist.api.domain.report.dto.ExecutiveConclusionResponse;
import com.aicoinassist.api.domain.report.dto.ExternalContextSummaryResponse;
import com.aicoinassist.api.domain.report.dto.ExternalRegimeSignalResponse;
import com.aicoinassist.api.domain.report.dto.HeroResponse;
import com.aicoinassist.api.domain.report.dto.IndicatorSnapshotResponse;
import com.aicoinassist.api.domain.report.dto.LevelSummaryResponse;
import com.aicoinassist.api.domain.report.dto.MarketParticipationSummaryResponse;
import com.aicoinassist.api.domain.report.dto.MacroSnapshotResponse;
import com.aicoinassist.api.domain.report.dto.MarketStructureBoxResponse;
import com.aicoinassist.api.domain.report.dto.ParticipationWindowMetricResponse;
import com.aicoinassist.api.domain.report.dto.PriceLevelResponse;
import com.aicoinassist.api.domain.report.dto.PriceZoneResponse;
import com.aicoinassist.api.domain.report.dto.RangeSnapshotResponse;
import com.aicoinassist.api.domain.report.dto.ReferenceNewsItemResponse;
import com.aicoinassist.api.domain.report.dto.ReportDetailResponse;
import com.aicoinassist.api.domain.report.dto.ReportHeaderResponse;
import com.aicoinassist.api.domain.report.dto.ReportHistoryItemResponse;
import com.aicoinassist.api.domain.report.dto.ReportHistoryResponse;
import com.aicoinassist.api.domain.report.dto.ReportMetaResponse;
import com.aicoinassist.api.domain.report.dto.ReportPageResponse;
import com.aicoinassist.api.domain.report.dto.ReportSignalHeadlineResponse;
import com.aicoinassist.api.domain.report.dto.ReportSnapshotResponse;
import com.aicoinassist.api.domain.report.dto.ReportSourceMetaResponse;
import com.aicoinassist.api.domain.report.dto.ReportSummaryResponse;
import com.aicoinassist.api.domain.report.dto.ScenarioViewResponse;
import com.aicoinassist.api.domain.report.dto.SharedContextSummaryResponse;
import com.aicoinassist.api.domain.report.dto.SentimentSnapshotResponse;
import com.aicoinassist.api.domain.report.dto.ValueLabelBasisResponse;
import com.aicoinassist.api.domain.report.dto.ZoneInteractionResponse;
import com.aicoinassist.api.domain.report.enumtype.ReportType;
import com.aicoinassist.api.domain.report.exception.ReportNotFoundException;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.repository.MarketIndicatorSnapshotRepository;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportNarrativeEntity;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportSharedContextEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportNarrativeRepository;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportReadService {

	private static final int DISPLAY_SCALE = 2;

	private final AnalysisReportRepository analysisReportRepository;
	private final AnalysisReportNarrativeRepository analysisReportNarrativeRepository;
	private final MarketIndicatorSnapshotRepository marketIndicatorSnapshotRepository;
	private final ObjectMapper objectMapper;

	public ReportDetailResponse getLatestDetail(String symbol, ReportType reportType) {
		return toDetailResponse(getLatestReport(symbol, toBatchReportType(reportType)));
	}

	public ReportSummaryResponse getLatestSummary(String symbol, ReportType reportType) {
		return toSummaryResponse(getLatestReport(symbol, toBatchReportType(reportType)));
	}

	public ReportDetailResponse getDetail(Long reportId) {
		AnalysisReportEntity report = analysisReportRepository.findById(reportId)
			.orElseThrow(() -> new ReportNotFoundException("Report not found: " + reportId));

		return toDetailResponse(report);
	}

	public ReportHistoryResponse getHistory(String symbol, ReportType reportType, int limit) {
		List<ReportHistoryItemResponse> items = analysisReportRepository
			.findBySymbolAndReportTypeOrderByAnalysisBasisTimeDescIdDesc(
				symbol,
				toBatchReportType(reportType),
				PageRequest.of(0, limit)
			)
			.stream()
			.map(this::toHistoryItem)
			.toList();

		return new ReportHistoryResponse(symbol, reportType, limit, items);
	}

	private ReportDetailResponse toDetailResponse(AnalysisReportEntity report) {
		ReportAssemblySource source = assembleSource(report);
		HeroResponse hero = buildHero(source.reportPayload(), source.narrativeOutput());

		return new ReportDetailResponse(
			buildMeta(source),
			buildHeader(source.reportPayload(), source.narrativeOutput()),
			new ReportPageResponse(
				hero,
				buildExecutiveConclusion(source.reportPayload(), source.narrativeOutput()),
				buildMarketParticipation(source),
				buildDomainAnalyses(source.narrativeOutput(), source.sharedContextOutput()),
				buildMarketStructureBox(toApiReportType(source.report().getReportType()), source.narrativeOutput()),
				buildCrossSignalIntegration(source.narrativeOutput()),
				buildScenarios(source.reportPayload(), source.narrativeOutput()),
				buildLevels(source.reportPayload()),
				buildExternalContext(source.reportPayload()),
				buildSharedContext(source.sharedContextOutput()),
				buildReferenceNews(source.referenceNews()),
				buildSourceMeta(source)
			)
		);
	}

	private ReportSummaryResponse toSummaryResponse(AnalysisReportEntity report) {
		ReportAssemblySource source = assembleSource(report);

		return new ReportSummaryResponse(
			buildMeta(source),
			buildHeader(source.reportPayload(), source.narrativeOutput()),
			buildHero(source.reportPayload(), source.narrativeOutput()),
			buildSnapshot(source),
			buildMarketParticipation(source),
			buildMarketStructureBox(toApiReportType(source.report().getReportType()), source.narrativeOutput()),
			buildSourceMeta(source)
		);
	}

	private ReportHistoryItemResponse toHistoryItem(AnalysisReportEntity report) {
		ReportAssemblySource source = assembleSource(report);
		ReportSnapshotResponse snapshot = buildSnapshot(source);

		return new ReportHistoryItemResponse(
			source.report().getId(),
			source.narrativeEntity().map(AnalysisReportNarrativeEntity::getId).orElse(null),
			source.report().getSymbol(),
			toApiReportType(source.report().getReportType()),
			source.report().getAnalysisBasisTime(),
			source.report().getRawReferenceTime(),
			priceSourceEventTime(source),
			source.report().getStoredTime(),
			text(source.reportPayload().path("summary"), "headline"),
			text(source.reportPayload().path("summary"), "outlook"),
			text(source.reportPayload().path("summary"), "confidence"),
			firstNonBlank(
				text(pathOrMissing(source.narrativeOutput(), "hero_summary"), "one_line_take"),
				text(pathOrMissing(source.narrativeOutput(), "executive_conclusion"), "summary")
			),
			firstNonBlank(
				text(pathOrMissing(source.narrativeOutput(), "hero_summary"), "market_regime"),
				text(pathOrMissing(source.narrativeOutput(), "executive_conclusion"), "overall_tone")
			),
			snapshot.currentPrice(),
			snapshot.dailyPriceChangeRate(),
			snapshot.trendLabel(),
			snapshot.volatilityLabel(),
			source.narrativeEntity().isPresent()
		);
	}

	private AnalysisReportEntity getLatestReport(String symbol, AnalysisReportType reportType) {
		return analysisReportRepository
			.findTopBySymbolAndReportTypeOrderByAnalysisBasisTimeDescIdDesc(symbol, reportType)
			.orElseThrow(() -> new ReportNotFoundException(
				"Report not found: " + symbol + ", " + reportType
			));
	}

	private ReportAssemblySource assembleSource(AnalysisReportEntity report) {
		JsonNode reportPayload = readJson(report.getReportPayload(), "report payload");
		Optional<AnalysisReportNarrativeEntity> narrativeEntity = findLatestNarrative(report.getId());
		Optional<AnalysisReportSharedContextEntity> sharedContextEntity = narrativeEntity
			.map(AnalysisReportNarrativeEntity::getSharedContext);
		Optional<MarketIndicatorSnapshotEntity> indicatorSnapshot = findIndicatorSnapshot(report);
		JsonNode narrativeOutput = narrativeEntity
			.map(AnalysisReportNarrativeEntity::getOutputJson)
			.map(payload -> readJson(payload, "narrative output"))
			.orElse(null);
		JsonNode sharedContextOutput = sharedContextEntity
			.map(AnalysisReportSharedContextEntity::getOutputJson)
			.map(payload -> readJson(payload, "shared context output"))
			.orElse(null);
		JsonNode referenceNews = narrativeEntity.map(this::extractReferenceNews).orElse(null);
		return new ReportAssemblySource(
			report,
			reportPayload,
			narrativeEntity,
			sharedContextEntity,
			indicatorSnapshot,
			narrativeOutput,
			sharedContextOutput,
			referenceNews
		);
	}

	private ReportMetaResponse buildMeta(ReportAssemblySource source) {
		return new ReportMetaResponse(
			source.report().getId(),
			source.narrativeEntity().map(AnalysisReportNarrativeEntity::getId).orElse(null),
			source.report().getSymbol(),
			toApiReportType(source.report().getReportType()),
			source.report().getAnalysisBasisTime(),
			source.report().getRawReferenceTime(),
			priceSourceEventTime(source),
			source.report().getStoredTime(),
			source.narrativeEntity().isPresent()
		);
	}

	private ReportHeaderResponse buildHeader(JsonNode reportPayload, JsonNode narrativeOutput) {
		JsonNode heroSummary = pathOrMissing(narrativeOutput, "hero_summary");
		JsonNode executiveConclusion = pathOrMissing(narrativeOutput, "executive_conclusion");

		return new ReportHeaderResponse(
			text(reportPayload.path("summary"), "headline"),
			text(reportPayload.path("summary"), "outlook"),
			text(reportPayload.path("summary"), "confidence"),
			text(reportPayload.path("summary").path("keyMessage"), "primaryMessage"),
			text(reportPayload.path("summary").path("keyMessage"), "continuityMessage"),
			firstNonBlank(
				text(heroSummary, "market_regime"),
				text(executiveConclusion, "overall_tone"),
				text(reportPayload.path("summary"), "outlook")
			),
			firstNonBlank(
				text(heroSummary, "one_line_take"),
				text(executiveConclusion, "summary"),
				text(reportPayload.path("summary").path("keyMessage"), "primaryMessage")
			),
			mapSignalHeadlines(reportPayload.path("summary").path("signalHeadlines"))
		);
	}

	private HeroResponse buildHero(JsonNode reportPayload, JsonNode narrativeOutput) {
		JsonNode heroSummary = pathOrMissing(narrativeOutput, "hero_summary");
		JsonNode executiveConclusion = pathOrMissing(narrativeOutput, "executive_conclusion");

		return new HeroResponse(
			firstNonBlank(
				text(heroSummary, "market_regime"),
				text(executiveConclusion, "overall_tone"),
				text(reportPayload.path("summary"), "outlook")
			),
			firstNonBlank(
				text(heroSummary, "one_line_take"),
				text(executiveConclusion, "summary"),
				text(reportPayload.path("summary").path("keyMessage"), "primaryMessage")
			),
			firstNonBlank(
				text(heroSummary, "primary_driver"),
				text(reportPayload.path("summary").path("keyMessage"), "primaryMessage")
			),
			firstNonBlank(
				text(heroSummary, "risk_driver"),
				firstArrayText(reportPayload.path("riskFactors"), "summary", "title")
			)
		);
	}

	private ReportSourceMetaResponse buildSourceMeta(ReportAssemblySource source) {
		Optional<AnalysisReportSharedContextEntity> sharedContextEntity = source.sharedContextEntity();
		return new ReportSourceMetaResponse(
			source.report().getSourceDataVersion(),
			source.report().getAnalysisEngineVersion(),
			source.narrativeEntity().map(AnalysisReportNarrativeEntity::getLlmProvider)
				.orElseGet(() -> sharedContextEntity.map(AnalysisReportSharedContextEntity::getLlmProvider).orElse(null)),
			source.narrativeEntity().map(AnalysisReportNarrativeEntity::getLlmModel)
				.orElseGet(() -> sharedContextEntity.map(AnalysisReportSharedContextEntity::getLlmModel).orElse(null)),
			source.narrativeEntity().map(entity -> entity.getGenerationStatus().name())
				.orElseGet(() -> sharedContextEntity.map(entity -> entity.getGenerationStatus().name()).orElse(null)),
			source.narrativeEntity().map(AnalysisReportNarrativeEntity::isFallbackUsed)
				.orElseGet(() -> sharedContextEntity.map(AnalysisReportSharedContextEntity::isFallbackUsed).orElse(false)),
			source.narrativeEntity().map(AnalysisReportNarrativeEntity::getPromptTemplateVersion)
				.orElseGet(() -> sharedContextEntity.map(AnalysisReportSharedContextEntity::getPromptTemplateVersion).orElse(null)),
			source.narrativeEntity().map(AnalysisReportNarrativeEntity::getInputSchemaVersion)
				.orElseGet(() -> sharedContextEntity.map(AnalysisReportSharedContextEntity::getInputSchemaVersion).orElse(null)),
			source.narrativeEntity().map(AnalysisReportNarrativeEntity::getOutputSchemaVersion)
				.orElseGet(() -> sharedContextEntity.map(AnalysisReportSharedContextEntity::getOutputSchemaVersion).orElse(null)),
			sharedContextEntity.map(AnalysisReportSharedContextEntity::getId).orElse(null),
			sharedContextEntity.map(AnalysisReportSharedContextEntity::getContextVersion).orElse(null),
			sharedContextEntity.isPresent()
		);
	}

	private Optional<AnalysisReportNarrativeEntity> findLatestNarrative(Long reportId) {
		return analysisReportNarrativeRepository.findTopByAnalysisReportIdOrderByStoredAtDescIdDesc(reportId);
	}

	private ReportSnapshotResponse buildSnapshot(ReportAssemblySource source) {
		JsonNode reportPayload = source.reportPayload();
		JsonNode currentState = reportPayload.path("marketContext").path("currentState");
		JsonNode comparisonFactD1 = firstNonNull(
			findByField(reportPayload.path("comparisonFacts"), "reference", "D1"),
			firstArrayObject(reportPayload.path("comparisonFacts"))
		);
		JsonNode windowSummary7d = firstNonNull(
			findByField(reportPayload.path("windowSummaries"), "windowType", "LAST_7D"),
			firstArrayObject(reportPayload.path("windowSummaries"))
		);
		JsonNode sentimentContext = reportPayload.path("sentimentContext");
		JsonNode sentimentComparisonD1 = firstNonNull(
			findByField(sentimentContext.path("comparisonFacts"), "reference", "D1"),
			firstArrayObject(sentimentContext.path("comparisonFacts"))
		);
		JsonNode macroContext = reportPayload.path("macroContext");
		JsonNode macroComparisonD1 = firstNonNull(
			findByField(macroContext.path("comparisonFacts"), "reference", "D1"),
			firstArrayObject(macroContext.path("comparisonFacts"))
		);
		JsonNode representativeParticipationWindow = selectRepresentativeParticipationWindow(
			reportPayload.path("marketParticipationSummaries"),
			toApiReportType(source.report().getReportType())
		);

		return new ReportSnapshotResponse(
			decimal(currentState, "currentPrice"),
			decimal(comparisonFactD1, "priceChangeRate"),
			priceSourceEventTime(source),
			text(representativeParticipationWindow, "windowLabel"),
			decimal(representativeParticipationWindow, "quoteVolumeChangeRate"),
			decimal(representativeParticipationWindow, "tradeCountChangeRate"),
			decimal(representativeParticipationWindow, "takerBuyQuoteRatio"),
			decimal(representativeParticipationWindow, "takerBuyQuoteRatioDelta"),
			new RangeSnapshotResponse(
				text(windowSummary7d, "windowType"),
				instant(windowSummary7d, "windowStartTime"),
				instant(windowSummary7d, "windowEndTime"),
				latestCandleOpenTime(source),
				latestCandleCloseTime(source),
				decimal(windowSummary7d, "high"),
				decimal(windowSummary7d, "low"),
				decimal(windowSummary7d, "range"),
				decimal(windowSummary7d, "currentPositionInRange"),
				decimal(windowSummary7d, "distanceFromWindowHigh"),
				decimal(windowSummary7d, "reboundFromWindowLow")
			),
			new IndicatorSnapshotResponse(
				decimal(currentState.path("momentumState"), "rsi14"),
				decimal(comparisonFactD1, "rsiDelta"),
				text(currentState.path("momentumState"), "signalSummary")
			),
			new IndicatorSnapshotResponse(
				decimal(currentState.path("momentumState"), "macdHistogram"),
				decimal(comparisonFactD1, "macdHistogramDelta"),
				text(currentState.path("momentumState"), "signalSummary")
			),
			new SentimentSnapshotResponse(
				decimal(sentimentContext, "indexValue"),
				text(sentimentContext, "classification"),
				decimal(sentimentComparisonD1, "valueChange"),
				decimal(sentimentComparisonD1, "valueChangeRate")
			),
			new MacroSnapshotResponse(
				decimal(macroContext, "dxyProxyValue"),
				decimal(macroComparisonD1, "dxyProxyChangeRate"),
				decimal(macroContext, "us10yYieldValue"),
				decimal(macroComparisonD1, "us10yYieldChangeRate"),
				decimal(macroContext, "usdKrwValue"),
				decimal(macroComparisonD1, "usdKrwChangeRate")
			),
			text(currentState, "trendLabel"),
			text(currentState, "volatilityLabel"),
			text(currentState, "rangePositionLabel")
		);
	}

	private MarketParticipationSummaryResponse buildMarketParticipation(ReportAssemblySource source) {
		JsonNode reportPayload = source.reportPayload();
		JsonNode representativeWindow = selectRepresentativeParticipationWindow(
			reportPayload.path("marketParticipationSummaries"),
			toApiReportType(source.report().getReportType())
		);
		List<String> facts = participationFacts(reportPayload.path("marketParticipationFacts"));
		List<String> highlights = mergeOrderedStrings(
			stringList(reportPayload.path("summary").path("keyMessage").path("signalDetails")),
			stringList(reportPayload.path("marketContext").path("windowContext").path("highlightDetails"))
		);
		if ((representativeWindow == null || representativeWindow.isMissingNode())
			&& facts.isEmpty()
			&& highlights.isEmpty()) {
			return null;
		}
		Instant representativeOpenTime = firstNonNull(
			latestCandleOpenTime(source),
			instant(representativeWindow, "currentWindowStartTime")
		);
		Instant representativeCloseTime = firstNonNull(
			latestCandleCloseTime(source),
			instant(representativeWindow, "currentWindowEndTime")
		);

		return new MarketParticipationSummaryResponse(
			"거래 참여도",
			firstNonBlank(findRepresentativeFact(facts, toApiReportType(source.report().getReportType())), first(highlights)),
			text(representativeWindow, "windowLabel"),
			instant(representativeWindow, "currentWindowStartTime"),
			instant(representativeWindow, "currentWindowEndTime"),
			instant(representativeWindow, "previousWindowStartTime"),
			instant(representativeWindow, "previousWindowEndTime"),
			integer(representativeWindow, "sampleCount"),
			decimal(representativeWindow, "priceChangeRate"),
			decimal(representativeWindow, "quoteVolumeChangeRate"),
			decimal(representativeWindow, "tradeCountChangeRate"),
			decimal(representativeWindow, "takerBuyQuoteRatio"),
			decimal(representativeWindow, "takerBuyQuoteRatioDelta"),
			source.report().getAnalysisBasisTime(),
			priceSourceEventTime(source),
			representativeOpenTime,
			representativeCloseTime,
			highlights,
			facts,
			mapParticipationWindows(reportPayload.path("marketParticipationSummaries"))
		);
	}

	private ExecutiveConclusionResponse buildExecutiveConclusion(JsonNode reportPayload, JsonNode narrativeOutput) {
		JsonNode executiveConclusion = pathOrMissing(narrativeOutput, "executive_conclusion");
		if (executiveConclusion != null && executiveConclusion.isObject() && executiveConclusion.size() > 0) {
			return new ExecutiveConclusionResponse(
				text(executiveConclusion, "summary"),
				firstNonEmptyList(
					stringList(executiveConclusion.path("bullish_factors")),
					stringList(executiveConclusion.path("top_supporting_factors"))
				),
				firstNonEmptyList(
					stringList(executiveConclusion.path("bearish_factors")),
					stringList(executiveConclusion.path("top_risk_factors"))
				),
				firstNonBlank(
					text(executiveConclusion, "tactical_view"),
					text(reportPayload.path("summary").path("keyMessage"), "continuityMessage"),
					text(reportPayload.path("summary"), "outlook")
				)
			);
		}

		return new ExecutiveConclusionResponse(
			text(reportPayload.path("summary").path("keyMessage"), "primaryMessage"),
			stringList(reportPayload.path("summary").path("keyMessage").path("signalDetails")),
			stringListFromObjectArray(reportPayload.path("riskFactors"), "summary", "title"),
			firstNonBlank(
				text(reportPayload.path("summary").path("keyMessage"), "continuityMessage"),
				text(reportPayload.path("summary"), "outlook")
			)
		);
	}

	private List<DomainAnalysisResponse> buildDomainAnalyses(JsonNode narrativeOutput, JsonNode sharedContextOutput) {
		JsonNode domainAnalyses = pathOrMissing(narrativeOutput, "domain_analyses");
		List<DomainAnalysisResponse> responses = new ArrayList<>();
		if (domainAnalyses != null && domainAnalyses.isArray()) {
			for (JsonNode item : domainAnalyses) {
				responses.add(new DomainAnalysisResponse(
					text(item, "domain"),
					firstNonBlank(text(item, "status"), text(item, "current_signal")),
					text(item, "interpretation"),
					firstNonBlank(
						text(item, "watch_point"),
						text(item, "watchPoint"),
						text(item, "pressure"),
						first(stringList(item.path("caveats")))
					)
				));
			}
		}
		return mergeSharedContextDomains(responses, sharedContextOutput);
	}

	private MarketStructureBoxResponse buildMarketStructureBox(ReportType reportType, JsonNode narrativeOutput) {
		JsonNode marketStructureBox = pathOrMissing(narrativeOutput, "market_structure_box");
		if (marketStructureBox == null || !marketStructureBox.isObject() || marketStructureBox.isEmpty()) {
			return null;
		}
		String marketStructureWindowType = marketStructureWindowType(reportType);

		return new MarketStructureBoxResponse(
			text(marketStructureBox, "range_low"),
			text(marketStructureBox, "current_price"),
			text(marketStructureBox, "range_high"),
			mapValueLabelBasis(marketStructureBox.path("range_position"), marketStructureWindowType),
			mapValueLabelBasis(marketStructureBox.path("upside_reference")),
			mapValueLabelBasis(marketStructureBox.path("downside_reference")),
			mapValueLabelBasis(marketStructureBox.path("support_break_risk")),
			mapValueLabelBasis(marketStructureBox.path("resistance_break_risk")),
			text(marketStructureBox, "interpretation")
		);
	}

	private CrossSignalIntegrationResponse buildCrossSignalIntegration(JsonNode narrativeOutput) {
		JsonNode crossSignal = pathOrMissing(narrativeOutput, "cross_signal_integration");
		if (crossSignal == null || !crossSignal.isObject()) {
			return new CrossSignalIntegrationResponse(null, List.of(), null, null);
		}

		return new CrossSignalIntegrationResponse(
			firstNonBlank(
				text(crossSignal, "alignment_summary"),
				joinStrings(stringList(crossSignal.path("aligned_signals")))
			),
			stringList(crossSignal.path("dominant_drivers")),
			firstNonBlank(
				text(crossSignal, "conflict_summary"),
				joinStrings(stringList(crossSignal.path("conflicting_signals")))
			),
			firstNonBlank(
				text(crossSignal, "positioning_take"),
				text(crossSignal, "combined_structure")
			)
		);
	}

	private List<ScenarioViewResponse> buildScenarios(JsonNode reportPayload, JsonNode narrativeOutput) {
		JsonNode scenarioMap = pathOrMissing(narrativeOutput, "scenario_map");
		if (scenarioMap != null && scenarioMap.isArray() && !scenarioMap.isEmpty()) {
			List<ScenarioViewResponse> responses = new ArrayList<>();
			for (JsonNode item : scenarioMap) {
				responses.add(new ScenarioViewResponse(
					text(item, "scenario_type"),
					text(item, "title"),
					text(item, "condition"),
					firstNonBlank(text(item, "trigger"), first(stringList(item.path("triggers")))),
					firstNonBlank(text(item, "confirmation"), first(stringList(item.path("confirming_signals")))),
					firstNonBlank(text(item, "invalidation"), first(stringList(item.path("invalidation_signals")))),
					text(item, "interpretation")
				));
			}
			return responses;
		}

		JsonNode fallbackScenarios = reportPayload.path("scenarios");
		if (!fallbackScenarios.isArray()) {
			return List.of();
		}

		List<ScenarioViewResponse> responses = new ArrayList<>();
		for (JsonNode item : fallbackScenarios) {
			responses.add(new ScenarioViewResponse(
				text(item, "bias"),
				text(item, "title"),
				first(stringList(item.path("triggerConditions"))),
				first(stringList(item.path("triggerConditions"))),
				null,
				first(stringList(item.path("invalidationSignals"))),
				text(item, "pathSummary")
			));
		}
		return responses;
	}

	private List<ReferenceNewsItemResponse> buildReferenceNews(JsonNode referenceNews) {
		if (referenceNews == null || !referenceNews.isArray()) {
			return List.of();
		}

		List<ReferenceNewsItemResponse> responses = new ArrayList<>();
		for (JsonNode item : referenceNews) {
			responses.add(new ReferenceNewsItemResponse(
				text(item, "title"),
				text(item, "source"),
				instant(item, "published_at"),
				text(item, "url"),
				text(item, "why_it_matters"),
				text(item, "related_domain")
			));
		}
		return responses;
	}

	private JsonNode extractReferenceNews(AnalysisReportNarrativeEntity entity) {
		JsonNode narrativeOutput = readJson(entity.getOutputJson(), "narrative output");
		JsonNode narrativeReferenceNews = narrativeOutput.path("reference_news");
		if (narrativeReferenceNews.isArray() && !narrativeReferenceNews.isEmpty()) {
			return narrativeReferenceNews;
		}
		return readJson(entity.getReferenceNewsJson(), "reference news");
	}

	private LevelSummaryResponse buildLevels(JsonNode reportPayload) {
		return new LevelSummaryResponse(
			mapPriceLevels(reportPayload.path("supportLevels")),
			mapPriceLevels(reportPayload.path("resistanceLevels")),
			mapPriceZones(reportPayload.path("supportZones")),
			mapPriceZones(reportPayload.path("resistanceZones")),
			mapPriceZone(firstNonNull(
				objectOrNull(reportPayload.path("nearestSupportZone")),
				objectOrNull(reportPayload.path("marketContext").path("levelContext").path("nearestSupportZone"))
			)),
			mapPriceZone(firstNonNull(
				objectOrNull(reportPayload.path("nearestResistanceZone")),
				objectOrNull(reportPayload.path("marketContext").path("levelContext").path("nearestResistanceZone"))
			)),
			mapZoneInteractions(firstNonNull(
				arrayOrNull(reportPayload.path("zoneInteractionFacts")),
				arrayOrNull(reportPayload.path("marketContext").path("levelContext").path("zoneInteractionFacts"))
			))
		);
	}

	private ExternalContextSummaryResponse buildExternalContext(JsonNode reportPayload) {
		JsonNode externalContext = firstNonNull(
			objectOrNull(reportPayload.path("marketContext").path("externalContextComposite")),
			objectOrNull(reportPayload.path("externalContextComposite"))
		);
		if (externalContext == null) {
			return null;
		}
		return new ExternalContextSummaryResponse(
			decimal(externalContext, "compositeRiskScore"),
			text(externalContext, "dominantDirection"),
			text(externalContext, "highestSeverity"),
			integer(externalContext, "supportiveSignalCount"),
			integer(externalContext, "cautionarySignalCount"),
			integer(externalContext, "headwindSignalCount"),
			text(externalContext, "primarySignalCategory"),
			text(externalContext, "primarySignalTitle"),
			text(externalContext, "primarySignalDetail"),
			mapExternalSignals(externalContext.path("regimeSignals"))
		);
	}

	private SharedContextSummaryResponse buildSharedContext(JsonNode sharedContextOutput) {
		if (sharedContextOutput == null || !sharedContextOutput.isObject() || sharedContextOutput.isEmpty()) {
			return null;
		}
		return new SharedContextSummaryResponse(
			text(sharedContextOutput, "context_version"),
			text(sharedContextOutput, "shared_summary"),
			toSharedDomainAnalysis("MACRO", sharedContextOutput.path("macro")),
			toSharedDomainAnalysis("SENTIMENT", sharedContextOutput.path("sentiment"))
		);
	}

	private JsonNode readJson(String payload, String fieldName) {
		try {
			return objectMapper.readTree(payload);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("Failed to deserialize " + fieldName + ".", exception);
		}
	}

	private JsonNode pathOrMissing(JsonNode root, String fieldName) {
		return root == null ? null : root.path(fieldName);
	}

	private String text(JsonNode root, String fieldName) {
		if (root == null) {
			return null;
		}
		JsonNode node = root.path(fieldName);
		return node.isMissingNode() || node.isNull() ? null : node.asText();
	}

	private BigDecimal decimal(JsonNode root, String fieldName) {
		if (root == null) {
			return null;
		}
		JsonNode node = root.path(fieldName);
		if (node.isMissingNode() || node.isNull() || !node.isNumber()) {
			return null;
		}
		return displayDecimal(node.decimalValue());
	}

	private BigDecimal displayDecimal(BigDecimal value) {
		if (value == null) {
			return null;
		}
		return value.setScale(DISPLAY_SCALE, RoundingMode.DOWN).stripTrailingZeros();
	}

	private Integer integer(JsonNode root, String fieldName) {
		if (root == null) {
			return null;
		}
		JsonNode node = root.path(fieldName);
		if (node.isMissingNode() || node.isNull() || !node.canConvertToInt()) {
			return null;
		}
		return node.intValue();
	}

	private Instant instant(JsonNode root, String fieldName) {
		String value = text(root, fieldName);
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return Instant.parse(value);
		} catch (DateTimeParseException exception) {
			return null;
		}
	}

	private JsonNode findByField(JsonNode arrayNode, String fieldName, String expectedValue) {
		if (arrayNode == null || !arrayNode.isArray()) {
			return null;
		}
		for (JsonNode item : arrayNode) {
			if (expectedValue.equals(text(item, fieldName))) {
				return item;
			}
		}
		return null;
	}

	private JsonNode firstArrayObject(JsonNode arrayNode) {
		if (arrayNode == null || !arrayNode.isArray()) {
			return null;
		}
		for (JsonNode item : arrayNode) {
			if (item != null && !item.isNull()) {
				return item;
			}
		}
		return null;
	}

	private List<String> stringList(JsonNode arrayNode) {
		if (arrayNode == null || !arrayNode.isArray()) {
			return List.of();
		}
		List<String> values = new ArrayList<>();
		for (JsonNode item : arrayNode) {
			if (!item.isNull()) {
				values.add(item.asText());
			}
		}
		return values;
	}

	private List<String> stringListFromObjectArray(JsonNode arrayNode, String... fields) {
		if (arrayNode == null || !arrayNode.isArray()) {
			return List.of();
		}
		List<String> values = new ArrayList<>();
		for (JsonNode item : arrayNode) {
			String value = item.isObject() ? firstNonBlank(texts(item, fields)) : item.isNull() ? null : item.asText();
			if (value != null) {
				values.add(value);
			}
		}
		return values;
	}

	private List<ReportSignalHeadlineResponse> mapSignalHeadlines(JsonNode arrayNode) {
		if (arrayNode == null || !arrayNode.isArray()) {
			return List.of();
		}
		List<ReportSignalHeadlineResponse> values = new ArrayList<>();
		for (JsonNode item : arrayNode) {
			values.add(new ReportSignalHeadlineResponse(
				text(item, "category"),
				text(item, "title"),
				text(item, "detail"),
				text(item, "importance")
			));
		}
		return values;
	}

	private ValueLabelBasisResponse mapValueLabelBasis(JsonNode node) {
		return mapValueLabelBasis(node, null);
	}

	private ValueLabelBasisResponse mapValueLabelBasis(JsonNode node, String windowType) {
		if (node == null || !node.isObject() || node.isEmpty()) {
			return null;
		}
		return new ValueLabelBasisResponse(
			text(node, "value"),
			text(node, "label"),
			text(node, "basis"),
			windowType
		);
	}

	private String marketStructureWindowType(ReportType reportType) {
		if (reportType == null) {
			return null;
		}
		return switch (reportType) {
			case LONG_TERM -> "LAST_180D";
			case MID_TERM -> "LAST_30D";
			case SHORT_TERM -> "LAST_7D";
		};
	}

	private List<PriceLevelResponse> mapPriceLevels(JsonNode arrayNode) {
		if (arrayNode == null || !arrayNode.isArray()) {
			return List.of();
		}
		List<PriceLevelResponse> values = new ArrayList<>();
		for (JsonNode item : arrayNode) {
			if (item.isObject()) {
				values.add(new PriceLevelResponse(
					text(item, "label"),
					text(item, "sourceType"),
					decimal(item, "price"),
					decimal(item, "distanceFromCurrent"),
					decimal(item, "strengthScore"),
					instant(item, "referenceTime"),
					integer(item, "reactionCount"),
					integer(item, "clusterSize"),
					text(item, "rationale"),
					stringList(item.path("triggerFacts"))
				));
			}
		}
		return values;
	}

	private List<ParticipationWindowMetricResponse> mapParticipationWindows(JsonNode arrayNode) {
		if (arrayNode == null || !arrayNode.isArray()) {
			return List.of();
		}
		List<ParticipationWindowMetricResponse> values = new ArrayList<>();
		for (JsonNode item : arrayNode) {
			if (item.isObject() && hasParticipationMetrics(item)) {
				values.add(new ParticipationWindowMetricResponse(
					text(item, "windowLabel"),
					instant(item, "currentWindowStartTime"),
					instant(item, "currentWindowEndTime"),
					instant(item, "previousWindowStartTime"),
					instant(item, "previousWindowEndTime"),
					integer(item, "sampleCount"),
					decimal(item, "priceChangeRate"),
					decimal(item, "quoteVolumeChangeRate"),
					decimal(item, "tradeCountChangeRate"),
					decimal(item, "takerBuyQuoteRatio"),
					decimal(item, "takerBuyQuoteRatioDelta")
				));
			}
		}
		return values;
	}

	private List<PriceZoneResponse> mapPriceZones(JsonNode arrayNode) {
		if (arrayNode == null || !arrayNode.isArray()) {
			return List.of();
		}
		List<PriceZoneResponse> values = new ArrayList<>();
		for (JsonNode item : arrayNode) {
			PriceZoneResponse zone = mapPriceZone(item);
			if (zone != null) {
				values.add(zone);
			}
		}
		return values;
	}

	private PriceZoneResponse mapPriceZone(JsonNode node) {
		if (node == null || !node.isObject() || node.isEmpty()) {
			return null;
		}
		return new PriceZoneResponse(
			text(node, "zoneType"),
			integer(node, "zoneRank"),
			decimal(node, "representativePrice"),
			decimal(node, "zoneLow"),
			decimal(node, "zoneHigh"),
			decimal(node, "distanceFromCurrent"),
			decimal(node, "distanceToZone"),
			decimal(node, "strengthScore"),
			text(node, "interactionType"),
			text(node, "strongestLevelLabel"),
			text(node, "strongestSourceType"),
			integer(node, "levelCount"),
			integer(node, "recentTestCount"),
			integer(node, "recentRejectionCount"),
			integer(node, "recentBreakCount"),
			stringList(node.path("triggerFacts"))
		);
	}

	private List<ZoneInteractionResponse> mapZoneInteractions(JsonNode arrayNode) {
		if (arrayNode == null || !arrayNode.isArray()) {
			return List.of();
		}
		List<ZoneInteractionResponse> values = new ArrayList<>();
		for (JsonNode item : arrayNode) {
			if (item.isObject()) {
				values.add(new ZoneInteractionResponse(
					text(item, "zoneType"),
					integer(item, "zoneRank"),
					text(item, "interactionType"),
					text(item, "summary"),
					stringList(item.path("triggerFacts"))
				));
			}
		}
		return values;
	}

	private List<ExternalRegimeSignalResponse> mapExternalSignals(JsonNode arrayNode) {
		if (arrayNode == null || !arrayNode.isArray()) {
			return List.of();
		}
		List<ExternalRegimeSignalResponse> values = new ArrayList<>();
		for (JsonNode item : arrayNode) {
			if (item.isObject()) {
				values.add(new ExternalRegimeSignalResponse(
					text(item, "category"),
					text(item, "title"),
					text(item, "detail"),
					text(item, "direction"),
					text(item, "severity"),
					text(item, "basisLabel")
				));
			}
		}
		return values;
	}

	private String first(List<String> values) {
		return values == null || values.isEmpty() ? null : values.getFirst();
	}

	private List<String> participationFacts(JsonNode arrayNode) {
		if (arrayNode == null || !arrayNode.isArray()) {
			return List.of();
		}
		List<String> values = new ArrayList<>();
		for (JsonNode item : arrayNode) {
			String value = item.isObject()
				? firstNonBlank(
					text(item, "summary"),
					text(item, "detail"),
					text(item, "description"),
					text(item, "fact"),
					text(item, "text")
				)
				: item.isNull() ? null : item.asText();
			if (value != null && !value.isBlank()) {
				values.add(value);
			}
		}
		return values;
	}

	private String firstArrayText(JsonNode arrayNode, String... fields) {
		if (arrayNode == null || !arrayNode.isArray()) {
			return null;
		}
		for (JsonNode item : arrayNode) {
			String value = item.isObject() ? firstNonBlank(texts(item, fields)) : item.isNull() ? null : item.asText();
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	private String[] texts(JsonNode node, String... fields) {
		String[] values = new String[fields.length];
		for (int i = 0; i < fields.length; i++) {
			values[i] = text(node, fields[i]);
		}
		return values;
	}

	private String firstNonBlank(String... values) {
		if (values == null) {
			return null;
		}
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return null;
	}

	private List<String> firstNonEmptyList(List<String> primary, List<String> fallback) {
		return primary != null && !primary.isEmpty() ? primary : fallback == null ? List.of() : fallback;
	}

	private String joinStrings(List<String> values) {
		return values == null || values.isEmpty() ? null : String.join(", ", values);
	}

	private List<String> mergeOrderedStrings(List<String> primary, List<String> secondary) {
		List<String> merged = new ArrayList<>();
		if (primary != null) {
			for (String value : primary) {
				if (value != null && !value.isBlank() && !merged.contains(value)) {
					merged.add(value);
				}
			}
		}
		if (secondary != null) {
			for (String value : secondary) {
				if (value != null && !value.isBlank() && !merged.contains(value)) {
					merged.add(value);
				}
			}
		}
		return merged;
	}

	private JsonNode selectRepresentativeParticipationWindow(JsonNode arrayNode, ReportType reportType) {
		if (arrayNode == null || !arrayNode.isArray()) {
			return null;
		}
		for (String windowLabel : preferredParticipationWindowLabels(reportType)) {
			JsonNode matched = findByField(arrayNode, "windowLabel", windowLabel);
			if (matched != null && hasParticipationMetrics(matched)) {
				return matched;
			}
		}
		for (JsonNode item : arrayNode) {
			if (item != null && item.isObject() && hasParticipationMetrics(item)) {
				return item;
			}
		}
		return null;
	}

	private List<String> preferredParticipationWindowLabels(ReportType reportType) {
		if (reportType == null) {
			return List.of();
		}
		return switch (reportType) {
			case SHORT_TERM -> List.of("최근 6h", "최근 3h", "최근 24h");
			case MID_TERM -> List.of("최근 7d", "최근 3d", "최근 30d");
			case LONG_TERM -> List.of("최근 30d", "최근 90d", "최근 180d");
		};
	}

	private boolean hasParticipationMetrics(JsonNode node) {
		return decimal(node, "priceChangeRate") != null
			|| decimal(node, "quoteVolumeChangeRate") != null
			|| decimal(node, "tradeCountChangeRate") != null
			|| decimal(node, "takerBuyQuoteRatio") != null
			|| decimal(node, "takerBuyQuoteRatioDelta") != null;
	}

	private String findRepresentativeFact(List<String> facts, ReportType reportType) {
		if (facts == null || facts.isEmpty()) {
			return null;
		}
		for (String phrase : preferredParticipationFactPhrases(reportType)) {
			for (String fact : facts) {
				if (fact != null && fact.contains(phrase)) {
					return fact;
				}
			}
		}
		return first(facts);
	}

	private List<String> preferredParticipationFactPhrases(ReportType reportType) {
		if (reportType == null) {
			return List.of();
		}
		return switch (reportType) {
			case SHORT_TERM -> List.of("최근 6h", "최근 3h", "최근 24h");
			case MID_TERM -> List.of("최근 7d", "최근 3d", "최근 30d");
			case LONG_TERM -> List.of("최근 30d", "최근 90d", "최근 180d");
		};
	}

	private String windowLabel(String windowType) {
		if (windowType == null || windowType.isBlank()) {
			return null;
		}
		return switch (windowType) {
			case "LAST_1D" -> "1d";
			case "LAST_3D" -> "3d";
			case "LAST_7D" -> "7d";
			case "LAST_14D" -> "14d";
			case "LAST_30D" -> "30d";
			case "LAST_90D" -> "90d";
			case "LAST_180D" -> "180d";
			case "LAST_52W" -> "52w";
			default -> windowType.startsWith("LAST_") ? windowType.substring(5).toLowerCase() : windowType;
		};
	}

	private Optional<MarketIndicatorSnapshotEntity> findIndicatorSnapshot(AnalysisReportEntity report) {
		return marketIndicatorSnapshotRepository
			.findTopBySymbolAndIntervalValueAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
				report.getSymbol(),
				intervalValue(report.getReportType()),
				report.getAnalysisBasisTime()
			);
	}

	private Instant priceSourceEventTime(ReportAssemblySource source) {
		return source.indicatorSnapshot()
			.map(MarketIndicatorSnapshotEntity::getPriceSourceEventTime)
			.orElse(source.report().getRawReferenceTime());
	}

	private Instant latestCandleOpenTime(ReportAssemblySource source) {
		return source.indicatorSnapshot()
			.map(MarketIndicatorSnapshotEntity::getLatestCandleOpenTime)
			.orElse(null);
	}

	private Instant latestCandleCloseTime(ReportAssemblySource source) {
		Instant openTime = latestCandleOpenTime(source);
		if (openTime == null) {
			return null;
		}
		return openTime.plus(intervalDuration(toApiReportType(source.report().getReportType()))).minusMillis(1);
	}

	private Duration intervalDuration(ReportType reportType) {
		if (reportType == null) {
			return Duration.ZERO;
		}
		return switch (reportType) {
			case SHORT_TERM -> Duration.ofHours(1);
			case MID_TERM -> Duration.ofHours(4);
			case LONG_TERM -> Duration.ofDays(1);
		};
	}

	private String intervalValue(AnalysisReportType reportType) {
		if (reportType == null) {
			return null;
		}
		return switch (reportType) {
			case SHORT_TERM -> "1h";
			case MID_TERM -> "4h";
			case LONG_TERM -> "1d";
		};
	}

	private List<DomainAnalysisResponse> mergeSharedContextDomains(
		List<DomainAnalysisResponse> responses,
		JsonNode sharedContextOutput
	) {
		if (sharedContextOutput == null || !sharedContextOutput.isObject()) {
			return responses;
		}
		List<DomainAnalysisResponse> merged = new ArrayList<>(responses);
		mergeSharedDomain(merged, "MACRO", sharedContextOutput.path("macro"));
		mergeSharedDomain(merged, "SENTIMENT", sharedContextOutput.path("sentiment"));
		return merged;
	}

	private void mergeSharedDomain(List<DomainAnalysisResponse> responses, String domainName, JsonNode sharedDomainNode) {
		DomainAnalysisResponse sharedDomain = toSharedDomainAnalysis(domainName, sharedDomainNode);
		if (sharedDomain == null) {
			return;
		}
		for (int i = 0; i < responses.size(); i++) {
			DomainAnalysisResponse current = responses.get(i);
			if (domainName.equalsIgnoreCase(current.domain())) {
				responses.set(i, new DomainAnalysisResponse(
					current.domain(),
					firstNonBlank(current.status(), sharedDomain.status()),
					firstNonBlank(current.interpretation(), sharedDomain.interpretation()),
					firstNonBlank(current.watchPoint(), sharedDomain.watchPoint())
				));
				return;
			}
		}
		responses.add(sharedDomain);
	}

	private DomainAnalysisResponse toSharedDomainAnalysis(String domainName, JsonNode sharedDomainNode) {
		if (sharedDomainNode == null || !sharedDomainNode.isObject() || sharedDomainNode.isEmpty()) {
			return null;
		}
		return new DomainAnalysisResponse(
			domainName,
			text(sharedDomainNode, "status"),
			text(sharedDomainNode, "summary"),
			firstNonBlank(text(sharedDomainNode, "watch_point"), text(sharedDomainNode, "watchPoint"))
		);
	}

	private JsonNode arrayOrNull(JsonNode node) {
		return node != null && node.isArray() && !node.isEmpty() ? node : null;
	}

	private JsonNode objectOrNull(JsonNode node) {
		return node != null && node.isObject() && !node.isEmpty() ? node : null;
	}

	private JsonNode firstNonNull(JsonNode primary, JsonNode fallback) {
		return primary != null ? primary : fallback;
	}

	private Instant firstNonNull(Instant... values) {
		if (values == null) {
			return null;
		}
		for (Instant value : values) {
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	private record ReportAssemblySource(
		AnalysisReportEntity report,
		JsonNode reportPayload,
		Optional<AnalysisReportNarrativeEntity> narrativeEntity,
		Optional<AnalysisReportSharedContextEntity> sharedContextEntity,
		Optional<MarketIndicatorSnapshotEntity> indicatorSnapshot,
		JsonNode narrativeOutput,
		JsonNode sharedContextOutput,
		JsonNode referenceNews
	) {
	}

	private ReportType toApiReportType(AnalysisReportType reportType) {
		return ReportType.valueOf(reportType.name());
	}

	private AnalysisReportType toBatchReportType(ReportType reportType) {
		return AnalysisReportType.valueOf(reportType.name());
	}
}
