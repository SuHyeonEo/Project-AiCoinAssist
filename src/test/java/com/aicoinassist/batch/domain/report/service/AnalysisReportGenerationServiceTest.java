package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketContextWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.market.repository.MarketIndicatorSnapshotRepository;
import com.aicoinassist.batch.domain.market.service.MarketCandidateLevelSnapshotPersistenceService;
import com.aicoinassist.batch.domain.market.service.MarketContextSnapshotPersistenceService;
import com.aicoinassist.batch.domain.market.service.MarketContextWindowSummarySnapshotPersistenceService;
import com.aicoinassist.batch.domain.market.service.MarketWindowSummarySnapshotPersistenceService;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFactSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContextSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMarketContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisCurrentStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMomentumStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMovingAveragePositionPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceLevel;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportDraft;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisScenario;
import com.aicoinassist.batch.domain.report.dto.AnalysisSummaryKeyMessagePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowContextSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisConfidenceLevel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisDerivativeHighlightImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisDerivativeMetricType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisOutlookType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelSourceType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisRangePositionLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisRiskFactorType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisScenarioBias;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisTrendLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisVolatilityLabel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisReportGenerationServiceTest {

    @Mock
    private MarketIndicatorSnapshotRepository marketIndicatorSnapshotRepository;

    @Mock
    private AnalysisComparisonService analysisComparisonService;

    @Mock
    private MarketCandidateLevelSnapshotPersistenceService marketCandidateLevelSnapshotPersistenceService;

    @Mock
    private MarketContextSnapshotPersistenceService marketContextSnapshotPersistenceService;

    @Mock
    private MarketContextWindowSummarySnapshotPersistenceService marketContextWindowSummarySnapshotPersistenceService;

    @Mock
    private MarketWindowSummarySnapshotPersistenceService marketWindowSummarySnapshotPersistenceService;

    @Mock
    private AnalysisReportContinuityService analysisReportContinuityService;

    @Mock
    private AnalysisDerivativeComparisonService analysisDerivativeComparisonService;

    @Mock
    private AnalysisReportAssembler analysisReportAssembler;

    @Mock
    private AnalysisReportPersistenceService analysisReportPersistenceService;

    @Test
    void generateAndSaveBuildsDraftFromLatestMappedSnapshot() {
        AnalysisReportGenerationService service = new AnalysisReportGenerationService(
                marketIndicatorSnapshotRepository,
                marketCandidateLevelSnapshotPersistenceService,
                marketContextSnapshotPersistenceService,
                marketContextWindowSummarySnapshotPersistenceService,
                marketWindowSummarySnapshotPersistenceService,
                analysisComparisonService,
                analysisDerivativeComparisonService,
                analysisReportContinuityService,
                analysisReportAssembler,
                analysisReportPersistenceService,
                new ObjectMapper()
        );

        MarketIndicatorSnapshotEntity snapshot = snapshot("4h");
        List<AnalysisComparisonFact> comparisonFacts = List.of(
                new AnalysisComparisonFact(
                        AnalysisComparisonReference.D1,
                        Instant.parse("2026-03-08T00:59:59Z"),
                        new BigDecimal("86000"),
                        new BigDecimal("1.7442"),
                        new BigDecimal("7"),
                        new BigDecimal("10"),
                        new BigDecimal("15.3846")
                )
        );
        AnalysisDerivativeContext derivativeContextInput = new AnalysisDerivativeContext(
                Instant.parse("2026-03-09T00:59:30Z"),
                Instant.parse("2026-03-09T00:59:00Z"),
                Instant.parse("2026-03-09T00:59:30Z"),
                "context-basis-key",
                new BigDecimal("12345.67890000"),
                new BigDecimal("87500.12000000"),
                new BigDecimal("87480.02000000"),
                new BigDecimal("0.00045000"),
                Instant.parse("2026-03-09T08:00:00Z"),
                new BigDecimal("0.12000000"),
                List.of(new AnalysisDerivativeComparisonFact(
                        AnalysisComparisonReference.D7,
                        Instant.parse("2026-03-02T00:59:30Z"),
                        new BigDecimal("11800.00000000"),
                        new BigDecimal("0.04624400"),
                        new BigDecimal("0.00014000"),
                        new BigDecimal("0.03500000")
                )),
                List.of(new AnalysisDerivativeWindowSummary(
                        MarketWindowType.LAST_30D,
                        Instant.parse("2026-02-07T00:59:30Z"),
                        Instant.parse("2026-03-09T00:59:30Z"),
                        180,
                        new BigDecimal("11000.00000000"),
                        new BigDecimal("0.12233445"),
                        new BigDecimal("0.00025000"),
                        new BigDecimal("0.80000000"),
                        new BigDecimal("0.07000000"),
                        new BigDecimal("0.71428571")
                )),
                List.of()
        );
        List<AnalysisPriceLevel> supportLevels = List.of(
                new AnalysisPriceLevel(
                        AnalysisPriceLevelLabel.MA20,
                        AnalysisPriceLevelSourceType.MOVING_AVERAGE,
                        new BigDecimal("87000"),
                        new BigDecimal("0.00571429"),
                        new BigDecimal("0.64428571"),
                        Instant.parse("2026-03-09T00:59:59Z"),
                        2,
                        1,
                        "Short-term average support",
                        List.of("Current price 87500 vs MA20 87000", "SUPPORT distance 0.57%")
                ),
                new AnalysisPriceLevel(
                        AnalysisPriceLevelLabel.MA60,
                        AnalysisPriceLevelSourceType.MOVING_AVERAGE,
                        new BigDecimal("86000"),
                        new BigDecimal("0.01714286"),
                        new BigDecimal("0.78285714"),
                        Instant.parse("2026-03-09T00:59:59Z"),
                        3,
                        2,
                        "Mid-trend average support",
                        List.of("Current price 87500 vs MA60 86000", "SUPPORT distance 1.71%")
                )
        );
        List<AnalysisPriceLevel> resistanceLevels = List.of(
                new AnalysisPriceLevel(
                        AnalysisPriceLevelLabel.BB_UPPER,
                        AnalysisPriceLevelSourceType.BOLLINGER_BAND,
                        new BigDecimal("88500"),
                        new BigDecimal("0.01142857"),
                        new BigDecimal("0.63857143"),
                        Instant.parse("2026-03-09T00:59:59Z"),
                        1,
                        1,
                        "Upper Bollinger band resistance",
                        List.of("Current price 87500 vs BB_UPPER 88500", "RESISTANCE distance 1.14%")
                )
        );
        AnalysisReportPayload payload = new AnalysisReportPayload(
                new AnalysisSummaryPayload(
                        "MID_TERM view",
                        AnalysisOutlookType.CONSTRUCTIVE,
                        AnalysisConfidenceLevel.HIGH,
                        new AnalysisSummaryKeyMessagePayload(
                                "summary",
                                List.of("signal detail"),
                                "continuity"
                        ),
                        List.of(
                                new AnalysisContextHeadlinePayload(AnalysisContextHeadlineCategory.COMPARISON, "D7 comparison", "detail", AnalysisContextHeadlineImportance.MEDIUM),
                                new AnalysisContextHeadlinePayload(AnalysisContextHeadlineCategory.WINDOW, "LAST_30D position", "detail", AnalysisContextHeadlineImportance.MEDIUM),
                                new AnalysisContextHeadlinePayload(AnalysisContextHeadlineCategory.DERIVATIVE, "D7 derivative shift", "detail", AnalysisContextHeadlineImportance.MEDIUM)
                        )
                ),
                new AnalysisMarketContextPayload(
                        new AnalysisCurrentStatePayload(
                                new BigDecimal("87500"),
                                AnalysisTrendLabel.BULLISH,
                                AnalysisVolatilityLabel.MODERATE,
                                AnalysisRangePositionLabel.UPPER_RANGE,
                                List.of(
                                        new AnalysisMovingAveragePositionPayload("MA20", new BigDecimal("87000"), true),
                                        new AnalysisMovingAveragePositionPayload("MA60", new BigDecimal("86000"), true),
                                        new AnalysisMovingAveragePositionPayload("MA120", new BigDecimal("85000"), true)
                                ),
                                new AnalysisMomentumStatePayload(
                                        new BigDecimal("62"),
                                        new BigDecimal("20"),
                                        "RSI14 62, MACD histogram 20"
                                )
                        ),
                        new AnalysisComparisonContextPayload(
                                new AnalysisContextHeadlinePayload(AnalysisContextHeadlineCategory.COMPARISON, "D7 comparison", "detail", AnalysisContextHeadlineImportance.MEDIUM),
                                new AnalysisComparisonFactSummaryPayload(
                                        "context comparison",
                                        List.of("comparison breakdown")
                                ),
                                List.of("comparison highlight")
                        ),
                        new AnalysisWindowContextPayload(
                                new AnalysisContextHeadlinePayload(AnalysisContextHeadlineCategory.WINDOW, "LAST_30D position", "detail", AnalysisContextHeadlineImportance.MEDIUM),
                                new AnalysisWindowContextSummaryPayload(
                                        "context window range",
                                        "context window position",
                                        "context window volatility"
                                ),
                                List.of("window highlight")
                        ),
                        new AnalysisDerivativeContextSummaryPayload(
                                "context derivative",
                                "context derivative window",
                                List.of("context derivative highlight"),
                                List.of("context derivative risk"),
                                7L
                        ),
                        new AnalysisContextHeadlinePayload(AnalysisContextHeadlineCategory.DERIVATIVE, "D7 derivative shift", "detail", AnalysisContextHeadlineImportance.MEDIUM),
                        new AnalysisContinuityContextPayload(
                                AnalysisComparisonReference.PREV_MID_REPORT,
                                "continuity summary",
                                List.of("continuity summary"),
                                List.of()
                        )
                ),
                comparisonFacts,
                List.of(new AnalysisComparisonHighlight(
                        AnalysisComparisonReference.D1,
                        "D1 shows price +1.7442% versus the reference point.",
                        "D1 keeps RSI Δ +7 and MACD hist Δ +10."
                )),
                List.of(new AnalysisWindowHighlight(
                        MarketWindowType.LAST_30D,
                        "LAST_30D keeps price at 68.75% of the range.",
                        "LAST_30D volume vs average +22%, ATR vs average +3.45%, distance from range high 2.78%."
                )),
                List.of(new AnalysisContinuityNote(
                        AnalysisComparisonReference.PREV_MID_REPORT,
                        Instant.parse("2026-03-01T20:59:59Z"),
                        "Previous mid-term report emphasized structure holding above weekly support."
                )),
                List.of(new AnalysisWindowSummary(
                        MarketWindowType.LAST_30D,
                        Instant.parse("2026-02-07T00:59:59Z"),
                        Instant.parse("2026-03-09T00:59:59Z"),
                        180,
                        new BigDecimal("90000"),
                        new BigDecimal("82000"),
                        new BigDecimal("8000"),
                        new BigDecimal("0.68750000"),
                        new BigDecimal("0.02777778"),
                        new BigDecimal("0.06707317"),
                        new BigDecimal("100.00000000"),
                        new BigDecimal("1450.00000000"),
                        new BigDecimal("0.22000000"),
                        new BigDecimal("0.03448276")
                )),
                new AnalysisDerivativeContext(
                        derivativeContextInput.snapshotTime(),
                        derivativeContextInput.openInterestSourceEventTime(),
                        derivativeContextInput.premiumIndexSourceEventTime(),
                        derivativeContextInput.sourceDataVersion(),
                        derivativeContextInput.openInterest(),
                        derivativeContextInput.markPrice(),
                        derivativeContextInput.indexPrice(),
                        derivativeContextInput.lastFundingRate(),
                        derivativeContextInput.nextFundingTime(),
                        derivativeContextInput.markIndexBasisRate(),
                        derivativeContextInput.comparisonFacts(),
                        derivativeContextInput.windowSummaries(),
                        List.of(new AnalysisDerivativeHighlight(
                                "D7 derivative shift",
                                "D7 keeps OI +4.6244%, funding Δ +0.014%, basis Δ +0.035%.",
                                AnalysisDerivativeHighlightImportance.MEDIUM,
                                AnalysisDerivativeMetricType.OPEN_INTEREST,
                                AnalysisComparisonReference.D7,
                                null
                        ))
                ),
                supportLevels,
                resistanceLevels,
                List.of(),
                List.of(new AnalysisScenario(
                        "Base case",
                        AnalysisScenarioBias.BULLISH,
                        List.of("trigger"),
                        "description",
                        List.of("invalidation")
                ))
        );
        MarketContextSnapshotEntity marketContextSnapshotEntity = MarketContextSnapshotEntity.builder()
                                                                                             .symbol("BTCUSDT")
                                                                                             .snapshotTime(Instant.parse("2026-03-09T00:59:30Z"))
                                                                                             .openInterestSourceEventTime(Instant.parse("2026-03-09T00:59:00Z"))
                                                                                             .premiumIndexSourceEventTime(Instant.parse("2026-03-09T00:59:30Z"))
                                                                                             .sourceDataVersion("context-basis-key")
                                                                                             .openInterest(new BigDecimal("12345.67890000"))
                                                                                             .markPrice(new BigDecimal("87500.12000000"))
                                                                                             .indexPrice(new BigDecimal("87480.02000000"))
                                                                                             .lastFundingRate(new BigDecimal("0.00045000"))
                                                                                             .nextFundingTime(Instant.parse("2026-03-09T08:00:00Z"))
                                                                                             .markIndexBasisRate(new BigDecimal("0.12000000"))
                                                                                             .build();
        List<MarketWindowSummarySnapshotEntity> windowSummaryEntities = List.of(
                MarketWindowSummarySnapshotEntity.builder()
                                                 .symbol("BTCUSDT")
                                                 .intervalValue("4h")
                                                 .windowType(MarketWindowType.LAST_30D.name())
                                                 .windowStartTime(Instant.parse("2026-02-07T00:59:59Z"))
                                                 .windowEndTime(snapshot.getSnapshotTime())
                                                 .sampleCount(180)
                                                 .currentPrice(new BigDecimal("87500"))
                                                 .windowHigh(new BigDecimal("90000"))
                                                 .windowLow(new BigDecimal("82000"))
                                                 .windowRange(new BigDecimal("8000"))
                                                 .currentPositionInRange(new BigDecimal("0.68750000"))
                                                 .distanceFromWindowHigh(new BigDecimal("0.02777778"))
                                                 .reboundFromWindowLow(new BigDecimal("0.06707317"))
                                                 .averageVolume(new BigDecimal("100.00000000"))
                                                 .averageAtr(new BigDecimal("1450.00000000"))
                                                 .currentVolume(new BigDecimal("122.00000000"))
                                                 .currentAtr(new BigDecimal("1500.00000000"))
                                                 .currentVolumeVsAverage(new BigDecimal("0.22000000"))
                                                 .currentAtrVsAverage(new BigDecimal("0.03448276"))
                                                 .sourceDataVersion("basis-key;windowType=LAST_30D")
                                                 .build()
        );
        List<MarketContextWindowSummarySnapshotEntity> derivativeWindowSummaryEntities = List.of(
                MarketContextWindowSummarySnapshotEntity.builder()
                                                       .symbol("BTCUSDT")
                                                       .windowType(MarketWindowType.LAST_30D.name())
                                                       .windowStartTime(Instant.parse("2026-02-07T00:59:30Z"))
                                                       .windowEndTime(Instant.parse("2026-03-09T00:59:30Z"))
                                                       .sampleCount(180)
                                                       .currentOpenInterest(new BigDecimal("12345.67890000"))
                                                       .averageOpenInterest(new BigDecimal("11000.00000000"))
                                                       .currentOpenInterestVsAverage(new BigDecimal("0.12233445"))
                                                       .currentFundingRate(new BigDecimal("0.00045000"))
                                                       .averageFundingRate(new BigDecimal("0.00025000"))
                                                       .currentFundingVsAverage(new BigDecimal("0.80000000"))
                                                       .currentBasisRate(new BigDecimal("0.12000000"))
                                                       .averageBasisRate(new BigDecimal("0.07000000"))
                                                       .currentBasisVsAverage(new BigDecimal("0.71428571"))
                                                       .sourceDataVersion("context-basis-key;windowType=LAST_30D")
                                                       .build()
        );
        List<MarketCandidateLevelSnapshotEntity> candidateLevelEntities = List.of(
                candidateLevelEntity("SUPPORT", "MA20", "MOVING_AVERAGE", "87000", "0.00571429", "0.64428571", "Short-term average support", "[\"Current price 87500 vs MA20 87000\",\"SUPPORT distance 0.57%\"]"),
                candidateLevelEntity("SUPPORT", "MA60", "MOVING_AVERAGE", "86000", "0.01714286", "0.78285714", "Mid-trend average support", "[\"Current price 87500 vs MA60 86000\",\"SUPPORT distance 1.71%\"]"),
                candidateLevelEntity("RESISTANCE", "BB_UPPER", "BOLLINGER_BAND", "88500", "0.01142857", "0.63857143", "Upper Bollinger band resistance", "[\"Current price 87500 vs BB_UPPER 88500\",\"RESISTANCE distance 1.14%\"]")
        );
        AnalysisReportEntity savedEntity = AnalysisReportEntity.builder()
                                                               .symbol("BTCUSDT")
                                                               .reportType(AnalysisReportType.MID_TERM)
                                                               .analysisBasisTime(snapshot.getSnapshotTime())
                                                               .rawReferenceTime(snapshot.getPriceSourceEventTime())
                                                               .sourceDataVersion(snapshot.getSourceDataVersion())
                                                               .analysisEngineVersion("gpt-5.4")
                                                               .reportPayload("{\"summary\":\"summary\"}")
                                                               .storedTime(Instant.parse("2026-03-09T01:00:30Z"))
                                                               .build();

        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueOrderBySnapshotTimeDescIdDesc("BTCUSDT", "4h"))
                .thenReturn(Optional.of(snapshot));
        when(analysisReportContinuityService.buildNotes(
                "BTCUSDT",
                AnalysisReportType.MID_TERM,
                snapshot.getSnapshotTime()
        )).thenReturn(payload.continuityNotes());
        when(marketContextSnapshotPersistenceService.createAndSave("BTCUSDT"))
                .thenReturn(marketContextSnapshotEntity);
        when(analysisDerivativeComparisonService.buildFacts(marketContextSnapshotEntity, AnalysisReportType.MID_TERM))
                .thenReturn(payload.derivativeContext().comparisonFacts());
        when(marketContextWindowSummarySnapshotPersistenceService.createAndSaveForReportType(
                marketContextSnapshotEntity,
                AnalysisReportType.MID_TERM
        )).thenReturn(derivativeWindowSummaryEntities);
        when(marketWindowSummarySnapshotPersistenceService.createAndSaveForReportType(snapshot, AnalysisReportType.MID_TERM))
                .thenReturn(windowSummaryEntities);
        when(marketCandidateLevelSnapshotPersistenceService.createAndSaveAll(snapshot))
                .thenReturn(candidateLevelEntities);
        when(analysisComparisonService.buildFacts(snapshot, AnalysisReportType.MID_TERM)).thenReturn(comparisonFacts);
        when(analysisReportAssembler.assemble(
                eq(snapshot),
                eq(AnalysisReportType.MID_TERM),
                eq(comparisonFacts),
                eq(payload.windowSummaries()),
                eq(derivativeContextInput),
                eq(payload.continuityNotes()),
                anyList(),
                anyList()
        )).thenReturn(payload);
        when(analysisReportPersistenceService.save(org.mockito.ArgumentMatchers.any(AnalysisReportDraft.class)))
                .thenReturn(savedEntity);

        AnalysisReportEntity result = service.generateAndSave(
                "BTCUSDT",
                AnalysisReportType.MID_TERM,
                "gpt-5.4",
                Instant.parse("2026-03-09T01:00:30Z")
        );

        ArgumentCaptor<AnalysisReportDraft> draftCaptor = ArgumentCaptor.forClass(AnalysisReportDraft.class);
        verify(analysisReportPersistenceService).save(draftCaptor.capture());

        AnalysisReportDraft draft = draftCaptor.getValue();
        assertThat(draft.symbol()).isEqualTo("BTCUSDT");
        assertThat(draft.reportType()).isEqualTo(AnalysisReportType.MID_TERM);
        assertThat(draft.analysisBasisTime()).isEqualTo(snapshot.getSnapshotTime());
        assertThat(draft.rawReferenceTime()).isEqualTo(snapshot.getPriceSourceEventTime());
        assertThat(draft.sourceDataVersion()).isEqualTo(snapshot.getSourceDataVersion());
        assertThat(draft.analysisEngineVersion()).isEqualTo("gpt-5.4");
        assertThat(draft.reportPayload()).isSameAs(payload);
        assertThat(draft.storedTime()).isEqualTo(Instant.parse("2026-03-09T01:00:30Z"));
        assertThat(result).isSameAs(savedEntity);
    }

    @Test
    void generateAndSaveFailsWhenNoSnapshotExistsForMappedInterval() {
        AnalysisReportGenerationService service = new AnalysisReportGenerationService(
                marketIndicatorSnapshotRepository,
                marketCandidateLevelSnapshotPersistenceService,
                marketContextSnapshotPersistenceService,
                marketContextWindowSummarySnapshotPersistenceService,
                marketWindowSummarySnapshotPersistenceService,
                analysisComparisonService,
                analysisDerivativeComparisonService,
                analysisReportContinuityService,
                analysisReportAssembler,
                analysisReportPersistenceService,
                new ObjectMapper()
        );

        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueOrderBySnapshotTimeDescIdDesc("BTCUSDT", "1d"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generateAndSave(
                "BTCUSDT",
                AnalysisReportType.LONG_TERM,
                "gpt-5.4",
                Instant.parse("2026-03-09T01:00:30Z")
        )).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("symbol=BTCUSDT")
          .hasMessageContaining("interval=1d");
    }

    private MarketIndicatorSnapshotEntity snapshot(String intervalValue) {
        return MarketIndicatorSnapshotEntity.builder()
                                            .symbol("BTCUSDT")
                                            .intervalValue(intervalValue)
                                            .snapshotTime(Instant.parse("2026-03-09T00:59:59Z"))
                                            .latestCandleOpenTime(Instant.parse("2026-03-08T23:59:59Z"))
                                            .priceSourceEventTime(Instant.parse("2026-03-09T00:59:30Z"))
                                            .sourceDataVersion("basis-key")
                                            .currentPrice(new BigDecimal("87500"))
                                            .ma20(new BigDecimal("87000"))
                                            .ma60(new BigDecimal("86000"))
                                            .ma120(new BigDecimal("85000"))
                                            .rsi14(new BigDecimal("62"))
                                            .macdLine(new BigDecimal("120"))
                                            .macdSignalLine(new BigDecimal("100"))
                                            .macdHistogram(new BigDecimal("20"))
                                            .atr14(new BigDecimal("1500"))
                                            .bollingerUpperBand(new BigDecimal("88500"))
                                            .bollingerMiddleBand(new BigDecimal("87000"))
                                            .bollingerLowerBand(new BigDecimal("85500"))
                                            .build();
    }

    private MarketCandidateLevelSnapshotEntity candidateLevelEntity(
            String levelType,
            String levelLabel,
            String sourceType,
            String levelPrice,
            String distanceFromCurrent,
            String strengthScore,
            String rationale,
            String triggerFactsPayload
    ) {
        return MarketCandidateLevelSnapshotEntity.builder()
                                                 .symbol("BTCUSDT")
                                                 .intervalValue("4h")
                                                 .snapshotTime(Instant.parse("2026-03-09T00:59:59Z"))
                                                 .referenceTime(Instant.parse("2026-03-09T00:59:59Z"))
                                                 .levelType(levelType)
                                                 .levelLabel(levelLabel)
                                                 .sourceType(sourceType)
                                                 .currentPrice(new BigDecimal("87500"))
                                                 .levelPrice(new BigDecimal(levelPrice))
                                                 .distanceFromCurrent(new BigDecimal(distanceFromCurrent))
                                                 .strengthScore(new BigDecimal(strengthScore))
                                                 .reactionCount(2)
                                                 .clusterSize(1)
                                                 .rationale(rationale)
                                                 .triggerFactsPayload(triggerFactsPayload)
                                                 .sourceDataVersion("basis-key;" + levelType + ";" + levelLabel)
                                                 .build();
    }
}
