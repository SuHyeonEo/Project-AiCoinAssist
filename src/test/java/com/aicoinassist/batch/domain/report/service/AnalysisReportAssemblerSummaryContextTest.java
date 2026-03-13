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
                richExternalContextComposite(),
                levelContext(),
                marketParticipationFacts(),
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
        assertThat(payload.summary().keyMessage().primaryMessage()).contains("상승 우위");
        assertThat(payload.summary().keyMessage().primaryMessage()).contains("거시 맥락에서 DXY는 평균 대비");
        assertThat(payload.summary().keyMessage().primaryMessage()).contains("심리 지표는 평균 대비");
        assertThat(payload.summary().keyMessage().primaryMessage()).contains("온체인 활동은 평균 대비");
        assertThat(payload.summary().keyMessage().primaryMessage()).contains("최근 30일 기준 외부 종합 리스크");
        assertThat(payload.summary().keyMessage().primaryMessage()).doesNotContainPattern("\\d+\\.\\d{3,}");
        assertThat(payload.summary().keyMessage().signalDetails()).anySatisfy(detail -> assertThat(detail).contains("가까운 지지"));
        assertThat(payload.summary().keyMessage().signalDetails()).anySatisfy(detail -> assertThat(detail).contains("가까운 저항"));
        assertThat(payload.summary().keyMessage().signalDetails()).anySatisfy(detail -> assertThat(detail).contains("최근 6h 기준 가격은"));
        assertThat(payload.summary().keyMessage().continuityMessage()).contains("이전 단기 리포트");
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
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.ONCHAIN, "D7 activity expansion"),
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.EXTERNAL, "TRANSITION_TO_HEADWIND")
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
        assertThat(payload.marketContext().comparisonContext().factSummary().primaryFact()).contains("PREV_BATCH 기준 가격은");
        assertThat(payload.marketContext().comparisonContext().factSummary().primaryFact()).contains("RSI 변화");
        assertThat(payload.marketContext().comparisonContext().factSummary().primaryFact()).doesNotContain("Δ", "ツ");
        assertThat(payload.marketContext().comparisonContext().factSummary().referenceBreakdown())
                .anySatisfy(detail -> assertThat(detail).contains("D1 기준 가격은"));
        assertThat(payload.marketContext().comparisonContext().highlightDetails()).isNotEmpty();
        assertThat(payload.marketContext().windowContext().headline()).extracting(
                        AnalysisContextHeadlinePayload::category,
                        AnalysisContextHeadlinePayload::title
                )
                .containsExactly(AnalysisContextHeadlineCategory.WINDOW, "LAST_7D position");
        assertThat(payload.marketContext().windowContext().summary().rangeSummary()).contains("최근 7일 레인지");
        assertThat(payload.marketContext().windowContext().summary().rangeSummary()).doesNotContainPattern("\\d+\\.\\d{3,}");
        assertThat(payload.marketContext().windowContext().summary().rangePositionSummary()).contains("현재 위치는 레인지의");
        assertThat(payload.marketContext().windowContext().summary().volatilitySummary()).contains("ATR은 평균 대비");
        assertThat(payload.marketContext().windowContext().highlightDetails()).isNotEmpty();
        assertThat(payload.marketContext().windowContext().highlightDetails()).anySatisfy(detail -> assertThat(detail).contains("최근 24h 기준 가격은"));
        assertThat(payload.marketContext().sentimentContextSummary().currentStateSummary()).contains("공포·탐욕 지수는 72");
        assertThat(payload.marketContext().sentimentContextSummary().comparisonSummary()).contains("탐욕");
        assertThat(payload.marketContext().sentimentContextSummary().windowSummary()).contains("최근 7일 기준");
        assertThat(payload.marketContext().sentimentContextSummary().highlightDetails()).isNotEmpty();
        assertThat(payload.marketContext().macroContextSummary().currentStateSummary()).contains("DXY 프록시는");
        assertThat(payload.marketContext().macroContextSummary().currentStateSummary()).contains("119.84", "1453.22");
        assertThat(payload.marketContext().macroContextSummary().currentStateSummary()).doesNotContainPattern("\\d+\\.\\d{3,}");
        assertThat(payload.marketContext().macroContextSummary().comparisonSummary()).contains("D30");
        assertThat(payload.marketContext().macroContextSummary().windowSummary()).contains("최근 30일 기준");
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
        assertThat(payload.marketContext().onchainContextSummary().currentStateSummary()).contains("활성 주소는");
        assertThat(payload.marketContext().onchainContextSummary().currentStateSummary()).doesNotContainPattern("\\d+\\.\\d{3,}");
        assertThat(payload.marketContext().onchainContextSummary().comparisonSummary()).contains("D7");
        assertThat(payload.marketContext().onchainContextSummary().windowSummary()).contains("최근 30일 기준");
        assertThat(payload.marketContext().onchainContextSummary().highlightDetails()).isNotEmpty();
        assertThat(payload.marketContext().onchainHeadline()).extracting(
                        AnalysisContextHeadlinePayload::category,
                        AnalysisContextHeadlinePayload::title
                )
                .containsExactly(AnalysisContextHeadlineCategory.ONCHAIN, "D7 activity expansion");
        assertThat(payload.marketContext().externalHeadline()).extracting(
                        AnalysisContextHeadlinePayload::category,
                        AnalysisContextHeadlinePayload::title
                )
                .containsExactly(AnalysisContextHeadlineCategory.EXTERNAL, "External regime direction changed");
        assertThat(payload.marketContext().externalContextComposite()).isNotNull();
        assertThat(payload.marketContext().externalContextComposite().primarySignalTitle()).isEqualTo("Dollar strength regime");
        assertThat(payload.marketContext().externalContextComposite().primarySignalDetail()).doesNotContain("representative averages");
        assertThat(payload.marketContext().externalContextComposite().compositeRiskScore()).isEqualByComparingTo("1.33333333");
        assertThat(payload.marketContext().externalContextComposite().comparisonFacts()).hasSize(1);
        assertThat(payload.marketContext().externalContextComposite().highlights()).hasSize(1);
        assertThat(payload.marketContext().externalContextComposite().windowSummaries()).hasSize(1);
        assertThat(payload.marketContext().externalContextComposite().transitions()).hasSize(1);
        assertThat(payload.marketContext().externalContextComposite().persistence()).isNotNull();
        assertThat(payload.marketContext().externalContextComposite().state()).isNotNull();
        assertThat(payload.marketContext().externalContextComposite().highlights())
                .allSatisfy(highlight -> assertThat(highlight.summary()).doesNotContain("shifted to", "changed from", "composite risk score"));
        assertThat(payload.marketContext().externalRegimeSignals()).extracting(
                        signal -> signal.category().name(),
                        signal -> signal.title()
                )
                .contains(
                        org.assertj.core.groups.Tuple.tuple("DERIVATIVE", "Funding crowding regime"),
                        org.assertj.core.groups.Tuple.tuple("MACRO", "Dollar strength regime"),
                        org.assertj.core.groups.Tuple.tuple("SENTIMENT", "Greed regime")
                );
        assertThat(payload.marketContext().externalRegimeSignals())
                .allSatisfy(signal -> assertThat(signal.detail()).doesNotContain("Fear & Greed is at", "versus average"));
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
                        "이전 단기 리포트에서는 모멘텀 지속 구도를 강조했습니다."
                );
        assertThat(payload.marketContext().continuityContext().carriedSignals())
                .containsExactly("이전 단기 리포트에서는 모멘텀 지속 구도를 강조했습니다.");
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
                                         .contains("SENTIMENT_GREED_EXTREME", "MACRO_VOLATILITY", "EXTERNAL_RISK_CONFLUENCE");
        assertThat(payload.riskFactors())
                .allSatisfy(riskFactor -> {
                    assertThat(riskFactor.summary()).doesNotContain("Fear & Greed is at", "Current funding rate", "Price is trading at");
                    assertThat(riskFactor.summary()).doesNotContainPattern("\\d+\\.\\d{3,}");
                    assertThat(riskFactor.triggerFacts()).allSatisfy(trigger ->
                            assertThat(trigger).doesNotContain("Current funding rate is", "Current sentiment is", "Current price is")
                                               .doesNotContainPattern("\\d+\\.\\d{3,}"));
                });
        assertThat(payload.scenarios())
                .allSatisfy(scenario -> {
                    assertThat(scenario.pathSummary()).doesNotContain("Price holds above", "Price stays below", "A loss of", "Failure to hold");
                    assertThat(scenario.pathSummary()).doesNotContainPattern("\\d+\\.\\d{3,}");
                    assertThat(scenario.triggerConditions()).allSatisfy(trigger ->
                            assertThat(trigger).doesNotContain("Price holds above", "Price stays below", "Momentum remains", "Price breaks beyond")
                                               .doesNotContainPattern("\\d+\\.\\d{3,}"));
                    assertThat(scenario.invalidationSignals()).allSatisfy(signal ->
                            assertThat(signal).doesNotContain("invalidates", "Failure to hold")
                                              .doesNotContainPattern("\\d+\\.\\d{3,}"));
                });
        assertThat(payload.summary().signalHeadlines()).anySatisfy(
                headline -> assertThat(headline.detail()).containsAnyOf("하방 부담", "외부")
        );
        assertThat(payload.summary().signalHeadlines()).allSatisfy(headline ->
                assertThat(headline.detail()).doesNotContain("keeps", "versus average", "regime", "pivot level")
        );
        assertThat(payload.summary().keyMessage().primaryMessage()).doesNotContain("企", "Δ", "ツ");
    }
}
