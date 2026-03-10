package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisCurrentStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisMomentumStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMovingAveragePositionPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowHighlight;
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

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisReportAssemblerTest extends AnalysisReportServiceFixtures {

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
                        summary -> summary.headline(),
                        summary -> summary.outlook(),
                        summary -> summary.confidence()
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
                .containsExactly(AnalysisTrendLabel.BULLISH, AnalysisVolatilityLabel.MODERATE, AnalysisRangePositionLabel.UPPER_RANGE);
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
        assertThat(payload.marketContext().comparisonContext().factSummary().referenceBreakdown())
                .anySatisfy(detail -> assertThat(detail).contains("D1 price"));
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
        assertThat(payload.marketContext().levelContext().comparisonFacts()).hasSize(2);
        assertThat(payload.marketContext().levelContext().highlights()).extracting(AnalysisLevelContextHighlight::reference)
                                                                       .containsExactly(
                                                                               AnalysisComparisonReference.PREV_BATCH,
                                                                               AnalysisComparisonReference.D1
                                                                       );

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
        assertThat(payload.marketContext().continuityContext().carriedSignals())
                .containsExactly("Previous short-term report highlighted a momentum continuation setup.");

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
        assertThat(payload.continuityNotes()).extracting(note -> note.reference())
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
        assertThat(payload.marketContext().currentState().rangePositionLabel()).isEqualTo(AnalysisRangePositionLabel.UPPER_RANGE);
        assertThat(payload.marketContext().windowContext().headline().title()).isEqualTo("LAST_52W position");
        assertThat(payload.marketContext().continuityContext().reference()).isEqualTo(AnalysisComparisonReference.PREV_LONG_REPORT);
    }
}
