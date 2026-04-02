package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisGptReportInputPayload;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisGptCrossSignalCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisGptReportInputAssemblerTest extends AnalysisReportPayloadTestFixtures {

    private final AnalysisGptReportInputAssembler assembler = new AnalysisGptReportInputAssembler(
            new AnalysisGptCrossSignalFactory()
    );

    @Test
    void assembleBuildsCompactGptInputWithCrossSignals() {
        AnalysisReportEntity entity = reportEntity(
                AnalysisReportType.SHORT_TERM,
                Instant.parse("2026-03-09T00:59:59Z"),
                Instant.parse("2026-03-09T00:59:30Z"),
                "snapshotTime=2026-03-09T00:59:59Z;latestCandleOpenTime=2026-03-08T23:59:59Z;priceSourceEventTime=2026-03-09T00:59:30Z",
                "gpt-5.4",
                "{\"summary\":\"unused\"}",
                Instant.parse("2026-03-09T01:00:30Z")
        );

        AnalysisGptReportInputPayload input = assembler.assemble(entity, shortTermPayload("Compact summary"));

        assertThat(input.symbol()).isEqualTo("BTCUSDT");
        assertThat(input.reportType()).isEqualTo(AnalysisReportType.SHORT_TERM);
        assertThat(input.summary().headline()).isEqualTo("SHORT_TERM view");
        assertThat(input.currentState()).isNotNull();
        assertThat(input.derivativeContext()).isNotNull();
        assertThat(input.macroContext()).isNotNull();
        assertThat(input.sentimentContext()).isNotNull();
        assertThat(input.onchainContext()).isNotNull();
        assertThat(input.externalContextComposite()).isNotNull();
        assertThat(input.signalHeadlines()).isNotEmpty();
        assertThat(input.primaryFacts()).isNotEmpty();
        assertThat(input.primaryFacts()).anySatisfy(fact -> assertThat(fact).contains("Compact summary"));
        assertThat(input.primaryFacts()).anySatisfy(fact -> assertThat(fact).contains("Volume vs average +22%"));
        assertThat(shortTermPayload("Compact summary").marketParticipationSummaries()).isNotEmpty();
        assertThat(shortTermPayload("Compact summary").marketParticipationSummaries().get(0).priceChangeRate())
                .isEqualByComparingTo("0.01240000");
        assertThat(input.marketParticipationFacts()).isNotEmpty();
        assertThat(input.marketParticipationFacts()).anySatisfy(fact -> assertThat(fact).contains("최근 6h 기준 가격은"));
        assertThat(input.marketParticipationFacts()).anySatisfy(fact -> assertThat(fact).contains("시장가 매수 비중은 56.67%"));
        assertThat(input.crossSignals()).extracting(signal -> signal.category())
                .containsExactly(
                        AnalysisGptCrossSignalCategory.MACRO_DERIVATIVE,
                        AnalysisGptCrossSignalCategory.SENTIMENT_DERIVATIVE,
                        AnalysisGptCrossSignalCategory.EXTERNAL_LEVEL,
                        AnalysisGptCrossSignalCategory.ONCHAIN_MACRO
                );
        assertThat(input.crossSignals()).anySatisfy(signal -> {
            if (signal.category() == AnalysisGptCrossSignalCategory.MACRO_DERIVATIVE) {
                assertThat(signal.supportingFacts()).anySatisfy(fact -> assertThat(fact).contains("Funding vs average"));
            }
        });
        assertThat(input.riskFactors()).isNotEmpty();
        assertThat(input.scenarios()).isNotEmpty();
        assertThat(input.continuityNotes()).isNotEmpty();
    }
}
