package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisZoneInteractionFact;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisDerivativeHighlightImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisDerivativeMetricType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceLevelSourceType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneInteractionType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisReportAssemblerDerivativeLevelTest extends AnalysisReportServiceFixtures {

    private final AnalysisReportAssembler assembler = new AnalysisReportAssembler();

    @Test
    void assembleBuildsDerivativeAndLevelSectionsFromStructuredFacts() {
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
                externalContextComposite(),
                levelContext(),
                supportLevels(),
                resistanceLevels(),
                supportZones(),
                resistanceZones()
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
        assertThat(payload.derivativeContext()).isNotNull();
        assertThat(payload.derivativeContext().lastFundingRate()).isEqualByComparingTo("0.00045000");
        assertThat(payload.derivativeContext().comparisonFacts()).hasSize(3);
        assertThat(payload.derivativeContext().windowSummaries()).hasSize(2);
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
    }
}
