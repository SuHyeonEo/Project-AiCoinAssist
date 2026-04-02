package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisRangePositionLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisReportAssemblerLongTermTest extends AnalysisReportServiceFixtures {

    private final AnalysisReportAssembler assembler = new AnalysisReportAssembler();

    @Test
    void assembleBuildsLongTermHighlightsFromCycleReferences() {
        AnalysisReportPayload payload = assembler.assemble(
                bullishSnapshot(),
                AnalysisReportType.LONG_TERM,
                longTermComparisonFacts(),
                longWindowSummaries(),
                derivativeContext(),
                macroContext(),
                sentimentContext(),
                onchainContext(),
                longContinuityNotes(),
                externalContextComposite(),
                levelContext(),
                marketParticipationFacts(),
                marketParticipationSummaries(),
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
        assertThat(payload.summary().keyMessage().continuityMessage()).contains("이전 장기 리포트");
        assertThat(payload.summary().signalHeadlines()).extracting(
                        AnalysisContextHeadlinePayload::category,
                        AnalysisContextHeadlinePayload::title
                )
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.COMPARISON, "Y52_HIGH comparison"),
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.WINDOW, "LAST_52W position"),
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.DERIVATIVE, "D180 derivative shift"),
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.MACRO, "Dollar strength regime"),
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.SENTIMENT, "Greed regime"),
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.ONCHAIN, "D30 activity contraction"),
                        org.assertj.core.groups.Tuple.tuple(AnalysisContextHeadlineCategory.EXTERNAL, "External regime direction changed")
                );
        assertThat(payload.marketContext().currentState().rangePositionLabel()).isEqualTo(AnalysisRangePositionLabel.UPPER_RANGE);
        assertThat(payload.marketContext().windowContext().headline().title()).isEqualTo("LAST_52W position");
        assertThat(payload.marketContext().continuityContext().reference()).isEqualTo(AnalysisComparisonReference.PREV_LONG_REPORT);
    }
}
