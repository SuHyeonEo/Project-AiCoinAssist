package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFactSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContextSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisCurrentStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMomentumStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMovingAveragePositionPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMarketContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceLevel;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceZone;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisSummaryKeyMessagePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowContextSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisZoneInteractionFact;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisConfidenceLevel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisDerivativeHighlightImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisDerivativeMetricType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisOutlookType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelSourceType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneInteractionType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisRangePositionLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisRiskFactorType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisScenarioBias;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisTrendLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisVolatilityLabel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisReportAssemblerTest {

    private final AnalysisReportAssembler assembler = new AnalysisReportAssembler();

    @Test
    void assembleBuildsBullishPayloadFromSnapshotFacts() {
        AnalysisReportPayload payload = assembler.assemble(
                bullishSnapshot(),
                AnalysisReportType.SHORT_TERM,
                comparisonFacts(),
                shortWindowSummaries(),
                derivativeContext(),
                shortContinuityNotes(),
                levelContext(),
                supportLevels(),
                resistanceLevels(),
                supportZones(),
                resistanceZones()
        );

        assertThat(payload.summary()).extracting(
                        AnalysisSummaryPayload::headline,
                        AnalysisSummaryPayload::outlook,
                        AnalysisSummaryPayload::confidence
                )
                .containsExactly("SHORT_TERM view", AnalysisOutlookType.CONSTRUCTIVE, AnalysisConfidenceLevel.HIGH);
        assertThat(payload.summary().keyMessage().primaryMessage()).contains("bullish");
        assertThat(payload.summary().keyMessage().signalDetails()).anySatisfy(detail -> assertThat(detail).contains("PREV_BATCH confirms the latest impulse"));
        assertThat(payload.summary().keyMessage().signalDetails()).anySatisfy(detail -> assertThat(detail).contains("Nearest support zone"));
        assertThat(payload.summary().keyMessage().signalDetails()).anySatisfy(detail -> assertThat(detail).contains("Nearest resistance zone"));
        assertThat(payload.summary().keyMessage().continuityMessage()).contains("Previous short-term report");
        assertThat(payload.summary().signalHeadlines()).extracting(
                        AnalysisContextHeadlinePayload::category,
                        AnalysisContextHeadlinePayload::title
                )
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.COMPARISON, "PREV_BATCH comparison"),
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.WINDOW, "LAST_7D position"),
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.DERIVATIVE, "PREV_BATCH derivative shift")
                );
        assertThat(payload.marketContext().currentState()).extracting(
                        AnalysisCurrentStatePayload::trendLabel,
                        AnalysisCurrentStatePayload::volatilityLabel,
                        AnalysisCurrentStatePayload::rangePositionLabel
                )
                .containsExactly(AnalysisTrendLabel.BULLISH, AnalysisVolatilityLabel.MODERATE, AnalysisRangePositionLabel.MID_RANGE);
        assertThat(payload.marketContext().currentState().movingAveragePositions()).extracting(
                        AnalysisMovingAveragePositionPayload::movingAverageName,
                        AnalysisMovingAveragePositionPayload::priceAbove
                )
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("MA20", true),
                        org.assertj.core.groups.Tuple.tuple("MA60", true),
                        org.assertj.core.groups.Tuple.tuple("MA120", true)
                );
        assertThat(payload.marketContext().currentState().momentumState()).extracting(
                        AnalysisMomentumStatePayload::rsi14,
                        AnalysisMomentumStatePayload::macdHistogram
                )
                .containsExactly(new BigDecimal("62"), new BigDecimal("20"));
        assertThat(payload.marketContext().comparisonContext().headline()).extracting(
                        AnalysisContextHeadlinePayload::category,
                        AnalysisContextHeadlinePayload::title
                )
                .containsExactly(AnalysisContextHeadlineCategory.COMPARISON, "PREV_BATCH comparison");
        assertThat(payload.marketContext().comparisonContext().factSummary().primaryFact()).contains("PREV_BATCH price");
        assertThat(payload.marketContext().comparisonContext().factSummary().referenceBreakdown()).anySatisfy(detail -> assertThat(detail).contains("D1 price"));
        assertThat(payload.marketContext().comparisonContext().highlightDetails()).isNotEmpty();
        assertThat(payload.marketContext().windowContext().headline()).extracting(
                        AnalysisContextHeadlinePayload::category,
                        AnalysisContextHeadlinePayload::title
                )
                .containsExactly(AnalysisContextHeadlineCategory.WINDOW, "LAST_7D position");
        assertThat(payload.marketContext().windowContext().summary().rangeSummary()).contains("LAST_7D range");
        assertThat(payload.marketContext().windowContext().summary().rangePositionSummary()).contains("position");
        assertThat(payload.marketContext().windowContext().summary().volatilitySummary()).contains("ATR vs average");
        assertThat(payload.marketContext().windowContext().highlightDetails()).isNotEmpty();
        assertThat(payload.marketContext().levelContext().supportBreakRisk()).isEqualByComparingTo("0.18000000");
        assertThat(payload.marketContext().levelContext().resistanceBreakRisk()).isEqualByComparingTo("0.05000000");
        assertThat(payload.marketContext().levelContext().zoneInteractionFacts()).hasSize(2);
        assertThat(payload.marketContext().derivativeContextSummary().currentStateSummary()).contains("Open interest");
        assertThat(payload.marketContext().derivativeHeadline()).extracting(
                        AnalysisContextHeadlinePayload::category,
                        AnalysisContextHeadlinePayload::title
                )
                .containsExactly(AnalysisContextHeadlineCategory.DERIVATIVE, "PREV_BATCH derivative shift");
        assertThat(payload.marketContext().derivativeContextSummary().windowSummary()).contains("LAST_7D");
        assertThat(payload.marketContext().derivativeContextSummary().highlightDetails()).isNotEmpty();
        assertThat(payload.marketContext().derivativeContextSummary().riskSignals()).isNotEmpty();
        assertThat(payload.marketContext().continuityContext()).extracting(
                        AnalysisContinuityContextPayload::reference,
                        AnalysisContinuityContextPayload::previousHeadline
                )
                .containsExactly(
                        AnalysisComparisonReference.PREV_SHORT_REPORT,
                        "Previous short-term report highlighted a momentum continuation setup."
                );
        assertThat(payload.marketContext().continuityContext().carriedSignals()).containsExactly(
                "Previous short-term report highlighted a momentum continuation setup."
        );
        assertThat(payload.derivativeContext()).isNotNull();
        assertThat(payload.derivativeContext().lastFundingRate()).isEqualByComparingTo("0.00045000");
        assertThat(payload.derivativeContext().comparisonFacts()).hasSize(3);
        assertThat(payload.derivativeContext().windowSummaries()).hasSize(2);
        assertThat(payload.derivativeContext().highlights()).hasSize(3);
        assertThat(payload.derivativeContext().highlights()).extracting(
                        AnalysisDerivativeHighlight::title,
                        AnalysisDerivativeHighlight::importance,
                        AnalysisDerivativeHighlight::relatedMetric
                )
                .contains(
                        org.assertj.core.groups.Tuple.tuple(
                                "PREV_BATCH derivative shift",
                                AnalysisDerivativeHighlightImportance.HIGH,
                                AnalysisDerivativeMetricType.OPEN_INTEREST
                        ),
                        org.assertj.core.groups.Tuple.tuple(
                                "LAST_7D derivative regime",
                                AnalysisDerivativeHighlightImportance.MEDIUM,
                                AnalysisDerivativeMetricType.FUNDING_RATE
                        ),
                        org.assertj.core.groups.Tuple.tuple(
                                "Funding crowding",
                                AnalysisDerivativeHighlightImportance.HIGH,
                                AnalysisDerivativeMetricType.FUNDING_RATE
                        )
                );
        assertThat(payload.comparisonFacts()).hasSize(2);
        assertThat(payload.windowSummaries()).hasSize(2);
        assertThat(payload.windowHighlights()).extracting(AnalysisWindowHighlight::windowType)
                                             .containsExactly(MarketWindowType.LAST_1D, MarketWindowType.LAST_7D);
        assertThat(payload.continuityNotes()).extracting(AnalysisContinuityNote::reference)
                                             .containsExactly(AnalysisComparisonReference.PREV_SHORT_REPORT);
        assertThat(payload.comparisonHighlights()).extracting(AnalysisComparisonHighlight::reference)
                                                 .containsExactly(
                                                         AnalysisComparisonReference.PREV_BATCH,
                                                         AnalysisComparisonReference.D1
                                                 );
        assertThat(payload.supportLevels()).extracting("label").contains(AnalysisPriceLevelLabel.MA20, AnalysisPriceLevelLabel.MA60);
        assertThat(payload.supportLevels()).allSatisfy(level -> {
            assertThat(level.sourceType()).isEqualTo(AnalysisPriceLevelSourceType.MOVING_AVERAGE);
            assertThat(level.distanceFromCurrent()).isNotNull();
            assertThat(level.strengthScore()).isNotNull();
            assertThat(level.triggerFacts()).isNotEmpty();
        });
        assertThat(payload.resistanceLevels()).extracting("label").contains(AnalysisPriceLevelLabel.BB_UPPER);
        assertThat(payload.resistanceLevels()).allSatisfy(level -> {
            assertThat(level.distanceFromCurrent()).isNotNull();
            assertThat(level.strengthScore()).isNotNull();
            assertThat(level.triggerFacts()).isNotEmpty();
        });
        assertThat(payload.supportZones()).hasSize(1);
        assertThat(payload.supportZones().get(0).includedLevelLabels()).contains(AnalysisPriceLevelLabel.MA20, AnalysisPriceLevelLabel.PIVOT_LOW);
        assertThat(payload.supportZones().get(0).interactionType()).isEqualTo(AnalysisPriceZoneInteractionType.ABOVE_ZONE);
        assertThat(payload.resistanceZones()).hasSize(1);
        assertThat(payload.resistanceZones().get(0).strongestLevelLabel()).isEqualTo(AnalysisPriceLevelLabel.PIVOT_HIGH);
        assertThat(payload.nearestSupportZone()).isEqualTo(payload.supportZones().get(0));
        assertThat(payload.nearestResistanceZone()).isEqualTo(payload.resistanceZones().get(0));
        assertThat(payload.zoneInteractionFacts()).extracting(
                        AnalysisZoneInteractionFact::zoneType,
                        AnalysisZoneInteractionFact::interactionType
                )
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(AnalysisPriceZoneType.SUPPORT, AnalysisPriceZoneInteractionType.ABOVE_ZONE),
                        org.assertj.core.groups.Tuple.tuple(AnalysisPriceZoneType.RESISTANCE, AnalysisPriceZoneInteractionType.BELOW_ZONE)
                );
        assertThat(payload.scenarios()).extracting("bias").contains(AnalysisScenarioBias.BULLISH, AnalysisScenarioBias.NEUTRAL);
        assertThat(payload.scenarios()).allSatisfy(scenario -> {
            assertThat(scenario.triggerConditions()).isNotEmpty();
            assertThat(scenario.pathSummary()).isNotBlank();
            assertThat(scenario.invalidationSignals()).isNotEmpty();
        });
    }

    @Test
    void assembleAddsRiskFactorsWhenSnapshotShowsExtensionAndVolatility() {
        AnalysisReportPayload payload = assembler.assemble(
                extendedSnapshot(),
                AnalysisReportType.SHORT_TERM,
                comparisonFacts(),
                shortWindowSummaries(),
                derivativeContext(),
                shortContinuityNotes(),
                levelContext(),
                supportLevels(),
                resistanceLevels(),
                supportZones(),
                resistanceZones()
        );

        assertThat(payload.riskFactors()).extracting("type", "title")
                                         .contains(
                                                 org.assertj.core.groups.Tuple.tuple(AnalysisRiskFactorType.RSI_OVERHEATING, "RSI overheating"),
                                                 org.assertj.core.groups.Tuple.tuple(AnalysisRiskFactorType.BAND_EXTENSION, "Band extension"),
                                                 org.assertj.core.groups.Tuple.tuple(AnalysisRiskFactorType.ELEVATED_VOLATILITY, "Elevated volatility"),
                                                 org.assertj.core.groups.Tuple.tuple(AnalysisRiskFactorType.FUNDING_SKEW, "Funding skew"),
                                                 org.assertj.core.groups.Tuple.tuple(AnalysisRiskFactorType.BASIS_EXPANSION, "Basis expansion")
                                         );
        assertThat(payload.riskFactors()).allSatisfy(riskFactor -> assertThat(riskFactor.triggerFacts()).isNotEmpty());
    }

    @Test
    void assembleBuildsLongTermHighlightsFromCycleReferences() {
        AnalysisReportPayload payload = assembler.assemble(
                bullishSnapshot(),
                AnalysisReportType.LONG_TERM,
                longTermComparisonFacts(),
                longWindowSummaries(),
                derivativeContext(),
                longContinuityNotes(),
                levelContext(),
                supportLevels(),
                resistanceLevels(),
                supportZones(),
                resistanceZones()
        );

        assertThat(payload.comparisonHighlights()).extracting(AnalysisComparisonHighlight::reference)
                                                 .containsExactly(
                                                         AnalysisComparisonReference.Y52_HIGH,
                                                         AnalysisComparisonReference.Y52_LOW,
                                                         AnalysisComparisonReference.D180
                                                 );
        assertThat(payload.summary().keyMessage().continuityMessage()).contains("Previous long-term");
        assertThat(payload.summary().signalHeadlines()).extracting(
                        AnalysisContextHeadlinePayload::category,
                        AnalysisContextHeadlinePayload::title
                )
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.COMPARISON, "Y52_HIGH comparison"),
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.WINDOW, "LAST_52W position"),
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.DERIVATIVE, "D180 derivative shift")
                );
        assertThat(payload.marketContext().comparisonContext().highlightDetails()).anySatisfy(detail -> assertThat(detail).contains("cycle floor"));
        assertThat(payload.windowHighlights()).extracting(AnalysisWindowHighlight::windowType)
                                             .containsExactly(MarketWindowType.LAST_180D, MarketWindowType.LAST_52W);
    }

    private List<AnalysisWindowSummary> shortWindowSummaries() {
        return List.of(
                new AnalysisWindowSummary(
                        MarketWindowType.LAST_1D,
                        Instant.parse("2026-03-08T00:59:59Z"),
                        Instant.parse("2026-03-09T00:59:59Z"),
                        24,
                        new BigDecimal("88000"),
                        new BigDecimal("86000"),
                        new BigDecimal("2000"),
                        new BigDecimal("0.75"),
                        new BigDecimal("0.0057"),
                        new BigDecimal("0.0174"),
                        new BigDecimal("100"),
                        new BigDecimal("1400"),
                        new BigDecimal("0.20"),
                        new BigDecimal("0.0714")
                ),
                new AnalysisWindowSummary(
                        MarketWindowType.LAST_7D,
                        Instant.parse("2026-03-02T00:59:59Z"),
                        Instant.parse("2026-03-09T00:59:59Z"),
                        168,
                        new BigDecimal("91000"),
                        new BigDecimal("82000"),
                        new BigDecimal("9000"),
                        new BigDecimal("0.61111111"),
                        new BigDecimal("0.03846154"),
                        new BigDecimal("0.06707317"),
                        new BigDecimal("100"),
                        new BigDecimal("1450"),
                        new BigDecimal("0.22"),
                        new BigDecimal("0.03448276")
                )
        );
    }

    private List<AnalysisWindowSummary> longWindowSummaries() {
        return List.of(
                new AnalysisWindowSummary(
                        MarketWindowType.LAST_180D,
                        Instant.parse("2025-09-10T00:59:59Z"),
                        Instant.parse("2026-03-09T00:59:59Z"),
                        180,
                        new BigDecimal("92000"),
                        new BigDecimal("52000"),
                        new BigDecimal("40000"),
                        new BigDecimal("0.8875"),
                        new BigDecimal("0.04891300"),
                        new BigDecimal("0.68269230"),
                        new BigDecimal("120"),
                        new BigDecimal("1300"),
                        new BigDecimal("0.15"),
                        new BigDecimal("0.1538")
                ),
                new AnalysisWindowSummary(
                        MarketWindowType.LAST_52W,
                        Instant.parse("2025-03-10T00:59:59Z"),
                        Instant.parse("2026-03-09T00:59:59Z"),
                        364,
                        new BigDecimal("92000"),
                        new BigDecimal("52000"),
                        new BigDecimal("40000"),
                        new BigDecimal("0.8875"),
                        new BigDecimal("0.04891300"),
                        new BigDecimal("0.68269230"),
                        new BigDecimal("140"),
                        new BigDecimal("1350"),
                        new BigDecimal("0.12"),
                        new BigDecimal("0.1111")
                )
        );
    }

    private MarketIndicatorSnapshotEntity bullishSnapshot() {
        return MarketIndicatorSnapshotEntity.builder()
                                            .symbol("BTCUSDT")
                                            .intervalValue("1h")
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

    private MarketIndicatorSnapshotEntity extendedSnapshot() {
        return MarketIndicatorSnapshotEntity.builder()
                                            .symbol("BTCUSDT")
                                            .intervalValue("1h")
                                            .snapshotTime(Instant.parse("2026-03-09T00:59:59Z"))
                                            .latestCandleOpenTime(Instant.parse("2026-03-08T23:59:59Z"))
                                            .priceSourceEventTime(Instant.parse("2026-03-09T00:59:30Z"))
                                            .sourceDataVersion("basis-key")
                                            .currentPrice(new BigDecimal("90000"))
                                            .ma20(new BigDecimal("87000"))
                                            .ma60(new BigDecimal("86000"))
                                            .ma120(new BigDecimal("85000"))
                                            .rsi14(new BigDecimal("74"))
                                            .macdLine(new BigDecimal("150"))
                                            .macdSignalLine(new BigDecimal("110"))
                                            .macdHistogram(new BigDecimal("40"))
                                            .atr14(new BigDecimal("3000"))
                                            .bollingerUpperBand(new BigDecimal("89500"))
                                            .bollingerMiddleBand(new BigDecimal("87000"))
                                            .bollingerLowerBand(new BigDecimal("84500"))
                                            .build();
    }

    private List<AnalysisComparisonFact> comparisonFacts() {
        return List.of(
                new AnalysisComparisonFact(
                        AnalysisComparisonReference.PREV_BATCH,
                        Instant.parse("2026-03-08T23:59:59Z"),
                        new BigDecimal("87000"),
                        new BigDecimal("0.5747"),
                        new BigDecimal("2"),
                        new BigDecimal("5"),
                        new BigDecimal("7.1429")
                ),
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
    }

    private List<AnalysisComparisonFact> longTermComparisonFacts() {
        return List.of(
                new AnalysisComparisonFact(
                        AnalysisComparisonReference.D180,
                        Instant.parse("2025-09-10T00:00:00Z"),
                        new BigDecimal("60000"),
                        new BigDecimal("45.8333"),
                        new BigDecimal("27"),
                        new BigDecimal("60"),
                        new BigDecimal("114.2857")
                ),
                new AnalysisComparisonFact(
                        AnalysisComparisonReference.Y52_HIGH,
                        Instant.parse("2025-11-20T00:00:00Z"),
                        new BigDecimal("92000"),
                        new BigDecimal("-4.8913"),
                        new BigDecimal("-6"),
                        new BigDecimal("-10"),
                        new BigDecimal("53.0612")
                ),
                new AnalysisComparisonFact(
                        AnalysisComparisonReference.Y52_LOW,
                        Instant.parse("2025-05-15T00:00:00Z"),
                        new BigDecimal("52000"),
                        new BigDecimal("68.2692"),
                        new BigDecimal("34"),
                        new BigDecimal("75"),
                        new BigDecimal("97.3684")
                )
        );
    }

    private List<AnalysisContinuityNote> shortContinuityNotes() {
        return List.of(new AnalysisContinuityNote(
                AnalysisComparisonReference.PREV_SHORT_REPORT,
                Instant.parse("2026-03-08T23:59:59Z"),
                "Previous short-term report highlighted a momentum continuation setup."
        ));
    }

    private List<AnalysisContinuityNote> longContinuityNotes() {
        return List.of(new AnalysisContinuityNote(
                AnalysisComparisonReference.PREV_LONG_REPORT,
                Instant.parse("2026-01-15T00:00:00Z"),
                "Previous long-term report emphasized cycle recovery above the major base."
        ));
    }

    private AnalysisDerivativeContext derivativeContext() {
        return new AnalysisDerivativeContext(
                Instant.parse("2026-03-09T00:59:30Z"),
                Instant.parse("2026-03-09T00:59:00Z"),
                Instant.parse("2026-03-09T00:59:30Z"),
                "openInterestSourceEventTime=2026-03-09T00:59:00Z;premiumIndexSourceEventTime=2026-03-09T00:59:30Z;nextFundingTime=2026-03-09T08:00:00Z",
                new BigDecimal("12345.67890000"),
                new BigDecimal("87500.12000000"),
                new BigDecimal("87480.02000000"),
                new BigDecimal("0.00045000"),
                Instant.parse("2026-03-09T08:00:00Z"),
                new BigDecimal("0.12000000"),
                derivativeComparisonFacts(),
                derivativeWindowSummaries(),
                List.of()
        );
    }

    private List<AnalysisDerivativeComparisonFact> derivativeComparisonFacts() {
        return List.of(
                new AnalysisDerivativeComparisonFact(
                        AnalysisComparisonReference.PREV_BATCH,
                        Instant.parse("2026-03-08T23:59:30Z"),
                        new BigDecimal("12000.00000000"),
                        new BigDecimal("0.02880658"),
                        new BigDecimal("0.00010000"),
                        new BigDecimal("0.02000000")
                ),
                new AnalysisDerivativeComparisonFact(
                        AnalysisComparisonReference.D1,
                        Instant.parse("2026-03-08T00:59:30Z"),
                        new BigDecimal("11800.00000000"),
                        new BigDecimal("0.04624400"),
                        new BigDecimal("0.00014000"),
                        new BigDecimal("0.03500000")
                ),
                new AnalysisDerivativeComparisonFact(
                        AnalysisComparisonReference.D180,
                        Instant.parse("2025-09-10T00:59:30Z"),
                        new BigDecimal("9000.00000000"),
                        new BigDecimal("0.37174210"),
                        new BigDecimal("0.00030000"),
                        new BigDecimal("0.08000000")
                )
        );
    }

    private List<AnalysisDerivativeWindowSummary> derivativeWindowSummaries() {
        return List.of(
                new AnalysisDerivativeWindowSummary(
                        MarketWindowType.LAST_7D,
                        Instant.parse("2026-03-02T00:59:30Z"),
                        Instant.parse("2026-03-09T00:59:30Z"),
                        42,
                        new BigDecimal("11000.00000000"),
                        new BigDecimal("0.12233445"),
                        new BigDecimal("0.00025000"),
                        new BigDecimal("0.80000000"),
                        new BigDecimal("0.07000000"),
                        new BigDecimal("0.71428571")
                ),
                new AnalysisDerivativeWindowSummary(
                        MarketWindowType.LAST_180D,
                        Instant.parse("2025-09-10T00:59:30Z"),
                        Instant.parse("2026-03-09T00:59:30Z"),
                        180,
                        new BigDecimal("9800.00000000"),
                        new BigDecimal("0.25976315"),
                        new BigDecimal("0.00018000"),
                        new BigDecimal("1.50000000"),
                        new BigDecimal("0.04000000"),
                        new BigDecimal("2.00000000")
                )
        );
    }

    private List<AnalysisPriceLevel> supportLevels() {
        return List.of(
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
    }

    private List<AnalysisPriceLevel> resistanceLevels() {
        return List.of(
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
    }

    private List<AnalysisPriceZone> supportZones() {
        return List.of(
                new AnalysisPriceZone(
                        AnalysisPriceZoneType.SUPPORT,
                        1,
                        new BigDecimal("86850"),
                        new BigDecimal("86850"),
                        new BigDecimal("87000"),
                        new BigDecimal("0.00742857"),
                        new BigDecimal("0.00514286"),
                        new BigDecimal("0.89285714"),
                        AnalysisPriceZoneInteractionType.ABOVE_ZONE,
                        AnalysisPriceLevelLabel.PIVOT_LOW,
                        AnalysisPriceLevelSourceType.PIVOT_LEVEL,
                        2,
                        5,
                        4,
                        1,
                        List.of(AnalysisPriceLevelLabel.MA20, AnalysisPriceLevelLabel.PIVOT_LOW),
                        List.of(AnalysisPriceLevelSourceType.MOVING_AVERAGE, AnalysisPriceLevelSourceType.PIVOT_LEVEL),
                        List.of("SUPPORT zone spans 86850 to 87000 with 2 candidate levels.", "Recent tests=5, rejections=4, breaks=1 within 14 days.")
                )
        );
    }

    private List<AnalysisPriceZone> resistanceZones() {
        return List.of(
                new AnalysisPriceZone(
                        AnalysisPriceZoneType.RESISTANCE,
                        1,
                        new BigDecimal("88560"),
                        new BigDecimal("88500"),
                        new BigDecimal("88620"),
                        new BigDecimal("0.01211429"),
                        new BigDecimal("0.01165714"),
                        new BigDecimal("0.86285714"),
                        AnalysisPriceZoneInteractionType.BELOW_ZONE,
                        AnalysisPriceLevelLabel.PIVOT_HIGH,
                        AnalysisPriceLevelSourceType.PIVOT_LEVEL,
                        2,
                        3,
                        2,
                        0,
                        List.of(AnalysisPriceLevelLabel.BB_UPPER, AnalysisPriceLevelLabel.PIVOT_HIGH),
                        List.of(AnalysisPriceLevelSourceType.BOLLINGER_BAND, AnalysisPriceLevelSourceType.PIVOT_LEVEL),
                        List.of("RESISTANCE zone spans 88500 to 88620 with 2 candidate levels.", "Recent tests=3, rejections=2, breaks=0 within 14 days.")
                )
        );
    }

    private AnalysisLevelContextPayload levelContext() {
        return new AnalysisLevelContextPayload(
                supportZones().get(0),
                resistanceZones().get(0),
                List.of(
                        new AnalysisZoneInteractionFact(
                                AnalysisPriceZoneType.SUPPORT,
                                1,
                                AnalysisPriceZoneInteractionType.ABOVE_ZONE,
                                "Nearest support zone is 86850 to 87000, currently above zone with 5 tests and break risk 18%.",
                                supportZones().get(0).triggerFacts()
                        ),
                        new AnalysisZoneInteractionFact(
                                AnalysisPriceZoneType.RESISTANCE,
                                1,
                                AnalysisPriceZoneInteractionType.BELOW_ZONE,
                                "Nearest resistance zone is 88500 to 88620, currently below zone with 3 tests and break risk 5%.",
                                resistanceZones().get(0).triggerFacts()
                        )
                ),
                new BigDecimal("0.18000000"),
                new BigDecimal("0.05000000")
        );
    }
}
