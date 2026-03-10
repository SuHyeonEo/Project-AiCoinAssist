package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketCandidateLevelZoneSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketContextWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketLevelContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.market.repository.MarketIndicatorSnapshotRepository;
import com.aicoinassist.batch.domain.market.service.MarketCandidateLevelSnapshotPersistenceService;
import com.aicoinassist.batch.domain.market.service.MarketCandidateLevelZoneSnapshotPersistenceService;
import com.aicoinassist.batch.domain.market.service.MarketContextSnapshotPersistenceService;
import com.aicoinassist.batch.domain.market.service.MarketContextWindowSummarySnapshotPersistenceService;
import com.aicoinassist.batch.domain.market.service.MarketLevelContextSnapshotPersistenceService;
import com.aicoinassist.batch.domain.market.service.MarketWindowSummarySnapshotPersistenceService;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFactSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.dto.AnalysisCurrentStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContextSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMarketContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMomentumStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMovingAveragePositionPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceLevel;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceZone;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisRiskFactor;
import com.aicoinassist.batch.domain.report.dto.AnalysisScenario;
import com.aicoinassist.batch.domain.report.dto.AnalysisZoneInteractionFact;
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
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneInteractionType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisRangePositionLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisScenarioBias;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisTrendLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisVolatilityLabel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

abstract class AnalysisReportGenerationServiceTestSupport extends AnalysisReportServiceFixtures {

    @Mock
    protected MarketIndicatorSnapshotRepository marketIndicatorSnapshotRepository;

    @Mock
    protected AnalysisComparisonService analysisComparisonService;

    @Mock
    protected AnalysisLevelContextComparisonService analysisLevelContextComparisonService;

    @Mock
    protected MarketCandidateLevelSnapshotPersistenceService marketCandidateLevelSnapshotPersistenceService;

    @Mock
    protected MarketCandidateLevelZoneSnapshotPersistenceService marketCandidateLevelZoneSnapshotPersistenceService;

    @Mock
    protected MarketLevelContextSnapshotPersistenceService marketLevelContextSnapshotPersistenceService;

    @Mock
    protected MarketContextSnapshotPersistenceService marketContextSnapshotPersistenceService;

    @Mock
    protected MarketContextWindowSummarySnapshotPersistenceService marketContextWindowSummarySnapshotPersistenceService;

    @Mock
    protected MarketWindowSummarySnapshotPersistenceService marketWindowSummarySnapshotPersistenceService;

    @Mock
    protected AnalysisReportContinuityService analysisReportContinuityService;

    @Mock
    protected AnalysisDerivativeComparisonService analysisDerivativeComparisonService;

    @Mock
    protected AnalysisReportAssembler analysisReportAssembler;

    @Mock
    protected AnalysisReportPersistenceService analysisReportPersistenceService;

    protected AnalysisReportGenerationService createService() {
        return new AnalysisReportGenerationService(
                marketIndicatorSnapshotRepository,
                marketCandidateLevelSnapshotPersistenceService,
                marketCandidateLevelZoneSnapshotPersistenceService,
                marketLevelContextSnapshotPersistenceService,
                marketContextSnapshotPersistenceService,
                marketContextWindowSummarySnapshotPersistenceService,
                marketWindowSummarySnapshotPersistenceService,
                analysisComparisonService,
                analysisLevelContextComparisonService,
                analysisDerivativeComparisonService,
                analysisReportContinuityService,
                analysisReportAssembler,
                analysisReportPersistenceService,
                new ObjectMapper()
        );
    }

    protected List<AnalysisComparisonFact> generationComparisonFacts() {
        return List.of(comparisonFacts().get(1));
    }

    protected List<AnalysisContinuityNote> midTermContinuityNotes() {
        return List.of(new AnalysisContinuityNote(
                AnalysisComparisonReference.PREV_MID_REPORT,
                Instant.parse("2026-03-01T20:59:59Z"),
                "Previous mid-term report emphasized structure holding above weekly support."
        ));
    }

    protected AnalysisDerivativeContext generationDerivativeContextInput() {
        return new AnalysisDerivativeContext(
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
                List.of(derivativeComparisonFacts().get(1)),
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
    }

    protected AnalysisReportPayload midTermPayload() {
        List<AnalysisPriceLevel> supportLevels = supportLevels();
        List<AnalysisPriceLevel> resistanceLevels = resistanceLevels();
        List<AnalysisPriceZone> supportZones = supportZones();
        List<AnalysisPriceZone> resistanceZones = resistanceZones();

        return new AnalysisReportPayload(
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
                        new AnalysisLevelContextPayload(
                                supportZones.get(0),
                                resistanceZones.get(0),
                                List.of(),
                                new BigDecimal("0.18000000"),
                                new BigDecimal("0.05000000"),
                                List.of(
                                        new AnalysisLevelContextComparisonFact(
                                                AnalysisComparisonReference.D7,
                                                Instant.parse("2026-03-02T00:59:59Z"),
                                                new BigDecimal("0.01200000"),
                                                new BigDecimal("-0.00400000"),
                                                new BigDecimal("0.05000000"),
                                                new BigDecimal("-0.02000000"),
                                                new BigDecimal("-0.03000000"),
                                                new BigDecimal("0.01000000"),
                                                AnalysisPriceZoneInteractionType.ABOVE_ZONE,
                                                AnalysisPriceZoneInteractionType.INSIDE_ZONE,
                                                AnalysisPriceZoneInteractionType.BELOW_ZONE,
                                                AnalysisPriceZoneInteractionType.BELOW_ZONE
                                        )
                                ),
                                List.of()
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
                generationComparisonFacts(),
                List.<AnalysisComparisonHighlight>of(),
                List.of(new AnalysisWindowHighlight(
                        MarketWindowType.LAST_30D,
                        "LAST_30D position",
                        "LAST_30D keeps price at 68.75% of the range."
                )),
                midTermContinuityNotes(),
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
                        generationDerivativeContextInput().snapshotTime(),
                        generationDerivativeContextInput().openInterestSourceEventTime(),
                        generationDerivativeContextInput().premiumIndexSourceEventTime(),
                        generationDerivativeContextInput().sourceDataVersion(),
                        generationDerivativeContextInput().openInterest(),
                        generationDerivativeContextInput().markPrice(),
                        generationDerivativeContextInput().indexPrice(),
                        generationDerivativeContextInput().lastFundingRate(),
                        generationDerivativeContextInput().nextFundingTime(),
                        generationDerivativeContextInput().markIndexBasisRate(),
                        generationDerivativeContextInput().comparisonFacts(),
                        generationDerivativeContextInput().windowSummaries(),
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
                supportZones,
                resistanceZones,
                supportZones.get(0),
                resistanceZones.get(0),
                List.<AnalysisZoneInteractionFact>of(),
                List.<AnalysisRiskFactor>of(),
                List.of(new AnalysisScenario(
                        "Base case",
                        AnalysisScenarioBias.BULLISH,
                        List.of("trigger"),
                        "description",
                        List.of("invalidation")
                ))
        );
    }

    protected MarketContextSnapshotEntity marketContextSnapshotEntity() {
        return MarketContextSnapshotEntity.builder()
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
    }

    protected List<MarketWindowSummarySnapshotEntity> windowSummaryEntities(MarketIndicatorSnapshotEntity snapshot) {
        return List.of(
                MarketWindowSummarySnapshotEntity.builder()
                                                 .symbol("BTCUSDT")
                                                 .intervalValue(snapshot.getIntervalValue())
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
    }

    protected List<MarketContextWindowSummarySnapshotEntity> derivativeWindowSummaryEntities() {
        return List.of(
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
    }

    protected List<MarketCandidateLevelSnapshotEntity> candidateLevelEntities() {
        return List.of(
                candidateLevelEntity("SUPPORT", "MA20", "MOVING_AVERAGE", "87000", "0.00571429", "0.64428571", "Short-term average support", "[\"Current price 87500 vs MA20 87000\",\"SUPPORT distance 0.57%\"]"),
                candidateLevelEntity("SUPPORT", "MA60", "MOVING_AVERAGE", "86000", "0.01714286", "0.78285714", "Mid-trend average support", "[\"Current price 87500 vs MA60 86000\",\"SUPPORT distance 1.71%\"]"),
                candidateLevelEntity("RESISTANCE", "BB_UPPER", "BOLLINGER_BAND", "88500", "0.01142857", "0.63857143", "Upper Bollinger band resistance", "[\"Current price 87500 vs BB_UPPER 88500\",\"RESISTANCE distance 1.14%\"]")
        );
    }

    protected List<MarketCandidateLevelZoneSnapshotEntity> candidateLevelZoneEntities() {
        return List.of(
                candidateLevelZoneEntity("SUPPORT", 1, "86850", "86850", "87000", "0.00742857", "0.00514286", "0.89285714", "ABOVE_ZONE", "PIVOT_LOW", "PIVOT_LEVEL", "[\"MA20\",\"PIVOT_LOW\"]", "[\"MOVING_AVERAGE\",\"PIVOT_LEVEL\"]"),
                candidateLevelZoneEntity("RESISTANCE", 1, "88560", "88500", "88620", "0.01211429", "0.01165714", "0.86285714", "BELOW_ZONE", "PIVOT_HIGH", "PIVOT_LEVEL", "[\"BB_UPPER\",\"PIVOT_HIGH\"]", "[\"BOLLINGER_BAND\",\"PIVOT_LEVEL\"]")
        );
    }

    protected MarketLevelContextSnapshotEntity levelContextSnapshotEntity() {
        return MarketLevelContextSnapshotEntity.builder()
                                              .symbol("BTCUSDT")
                                              .intervalValue("4h")
                                              .snapshotTime(Instant.parse("2026-03-09T00:59:59Z"))
                                              .currentPrice(new BigDecimal("87500"))
                                              .supportZoneRank(1)
                                              .supportRepresentativePrice(new BigDecimal("86850"))
                                              .supportZoneLow(new BigDecimal("86850"))
                                              .supportZoneHigh(new BigDecimal("87000"))
                                              .supportDistanceToZone(new BigDecimal("0.00514286"))
                                              .supportZoneStrength(new BigDecimal("0.89285714"))
                                              .supportInteractionType("ABOVE_ZONE")
                                              .supportRecentTestCount(5)
                                              .supportRecentRejectionCount(4)
                                              .supportRecentBreakCount(1)
                                              .supportBreakRisk(new BigDecimal("0.18000000"))
                                              .resistanceZoneRank(1)
                                              .resistanceRepresentativePrice(new BigDecimal("88560"))
                                              .resistanceZoneLow(new BigDecimal("88500"))
                                              .resistanceZoneHigh(new BigDecimal("88620"))
                                              .resistanceDistanceToZone(new BigDecimal("0.01165714"))
                                              .resistanceZoneStrength(new BigDecimal("0.86285714"))
                                              .resistanceInteractionType("BELOW_ZONE")
                                              .resistanceRecentTestCount(3)
                                              .resistanceRecentRejectionCount(2)
                                              .resistanceRecentBreakCount(0)
                                              .resistanceBreakRisk(new BigDecimal("0.05000000"))
                                              .sourceDataVersion("indicator=basis-key;supportZone=support;resistanceZone=resistance")
                                              .build();
    }

    protected AnalysisReportEntity savedReportEntity(MarketIndicatorSnapshotEntity snapshot) {
        return AnalysisReportEntity.builder()
                                   .symbol("BTCUSDT")
                                   .reportType(AnalysisReportType.MID_TERM)
                                   .analysisBasisTime(snapshot.getSnapshotTime())
                                   .rawReferenceTime(snapshot.getPriceSourceEventTime())
                                   .sourceDataVersion(snapshot.getSourceDataVersion())
                                   .analysisEngineVersion("gpt-5.4")
                                   .reportPayload("{\"summary\":\"summary\"}")
                                   .storedTime(Instant.parse("2026-03-09T01:00:30Z"))
                                   .build();
    }
}
