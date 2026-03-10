package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisCurrentStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisMomentumStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMovingAveragePositionPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowHighlight;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisConfidenceLevel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisOutlookType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisRangePositionLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisTrendLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisVolatilityLabel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisReportAssemblerSummaryContextTest extends AnalysisReportServiceFixtures {

    private final AnalysisReportAssembler assembler = new AnalysisReportAssembler();

    @Test
    void assembleBuildsBullishSummaryAndMarketContextFromSnapshotFacts() {
        AnalysisReportPayload payload = assembler.assemble(
                bullishSnapshot(),
                AnalysisReportType.SHORT_TERM,
                comparisonFacts(),
                shortWindowSummaries(),
                derivativeContext(),
                macroContext(),
                sentimentContext(),
                onchainContext(),
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
        assertThat(payload.summary().keyMessage().primaryMessage()).contains("Macro context keeps DXY");
        assertThat(payload.summary().keyMessage().primaryMessage()).contains("sentiment stays");
        assertThat(payload.summary().keyMessage().primaryMessage()).contains("on-chain activity runs");
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
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.DERIVATIVE, "PREV_BATCH derivative shift"),
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.MACRO, "Dollar strength regime"),
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.SENTIMENT, "Greed regime"),
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.ONCHAIN, "D7 activity expansion")
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
        assertThat(payload.marketContext().sentimentContextSummary().currentStateSummary()).contains("Fear & Greed 72");
        assertThat(payload.marketContext().sentimentContextSummary().comparisonSummary()).contains("Greed");
        assertThat(payload.marketContext().sentimentContextSummary().windowSummary()).contains("LAST_7D");
        assertThat(payload.marketContext().sentimentContextSummary().highlightDetails()).isNotEmpty();
        assertThat(payload.marketContext().macroContextSummary().currentStateSummary()).contains("DXY proxy");
        assertThat(payload.marketContext().macroContextSummary().comparisonSummary()).contains("D30");
        assertThat(payload.marketContext().macroContextSummary().windowSummary()).contains("LAST_30D");
        assertThat(payload.marketContext().macroContextSummary().highlightDetails()).isNotEmpty();
        assertThat(payload.marketContext().macroHeadline()).extracting(
                        AnalysisContextHeadlinePayload::category,
                        AnalysisContextHeadlinePayload::title
                )
                .containsExactly(AnalysisContextHeadlineCategory.MACRO, "Dollar strength regime");
        assertThat(payload.marketContext().sentimentHeadline()).extracting(
                        AnalysisContextHeadlinePayload::category,
                        AnalysisContextHeadlinePayload::title
                )
                .containsExactly(AnalysisContextHeadlineCategory.SENTIMENT, "Greed regime");
        assertThat(payload.marketContext().onchainContextSummary().currentStateSummary()).contains("Active addresses");
        assertThat(payload.marketContext().onchainContextSummary().comparisonSummary()).contains("D7");
        assertThat(payload.marketContext().onchainContextSummary().windowSummary()).contains("LAST_30D");
        assertThat(payload.marketContext().onchainContextSummary().highlightDetails()).isNotEmpty();
        assertThat(payload.marketContext().onchainHeadline()).extracting(
                        AnalysisContextHeadlinePayload::category,
                        AnalysisContextHeadlinePayload::title
                )
                .containsExactly(AnalysisContextHeadlineCategory.ONCHAIN, "D7 activity expansion");
        assertThat(payload.windowHighlights()).extracting(AnalysisWindowHighlight::windowType)
                                             .containsExactly(MarketWindowType.LAST_1D, MarketWindowType.LAST_7D);
        assertThat(payload.comparisonHighlights()).extracting(AnalysisComparisonHighlight::reference)
                                                 .containsExactly(
                                                         AnalysisComparisonReference.PREV_BATCH,
                                                         AnalysisComparisonReference.D1
                                                 );
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
        assertThat(payload.marketContext().levelContext().supportBreakRisk()).isEqualByComparingTo("0.18000000");
        assertThat(payload.marketContext().levelContext().resistanceBreakRisk()).isEqualByComparingTo("0.05000000");
        assertThat(payload.marketContext().levelContext().zoneInteractionFacts()).hasSize(2);
        assertThat(payload.marketContext().levelContext().comparisonFacts()).hasSize(2);
        assertThat(payload.marketContext().levelContext().highlights()).extracting(AnalysisLevelContextHighlight::reference)
                                                                       .containsExactly(
                                                                               AnalysisComparisonReference.PREV_BATCH,
                                                                               AnalysisComparisonReference.D1
                                                                       );
        assertThat(payload.sentimentContext()).isNotNull();
        assertThat(payload.macroContext()).isNotNull();
        assertThat(payload.onchainContext()).isNotNull();
        assertThat(payload.macroContext().highlights()).isNotEmpty();
        assertThat(payload.sentimentContext().highlights()).isNotEmpty();
        assertThat(payload.onchainContext().highlights()).isNotEmpty();
        assertThat(payload.riskFactors()).extracting(risk -> risk.type().name())
                                         .contains("SENTIMENT_GREED_EXTREME", "MACRO_VOLATILITY");
    }
}
