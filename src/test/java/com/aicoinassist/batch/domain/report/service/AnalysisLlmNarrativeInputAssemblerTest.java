package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisGptReportInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmDomainType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisLlmNarrativeInputAssemblerTest extends AnalysisReportPayloadTestFixtures {

    private final AnalysisGptReportInputAssembler gptAssembler = new AnalysisGptReportInputAssembler(
            new AnalysisGptCrossSignalFactory()
    );
    private final AnalysisLlmNarrativeInputAssembler assembler = new AnalysisLlmNarrativeInputAssembler();

    @Test
    void assembleBuildsNarrativeFriendlyLlmSchema() {
        AnalysisReportEntity entity = reportEntity(
                AnalysisReportType.SHORT_TERM,
                Instant.parse("2026-03-09T00:59:59Z"),
                Instant.parse("2026-03-09T00:59:30Z"),
                "snapshotTime=2026-03-09T00:59:59Z;latestCandleOpenTime=2026-03-08T23:59:59Z;priceSourceEventTime=2026-03-09T00:59:30Z",
                "gpt-5.4",
                "{\"summary\":\"unused\"}",
                Instant.parse("2026-03-09T01:00:30Z")
        );
        AnalysisGptReportInputPayload gptInput = gptAssembler.assemble(entity, shortTermPayload("Narrative summary"));

        AnalysisLlmNarrativeInputPayload llmInput = assembler.assemble(gptInput);

        assertThat(llmInput.symbol()).isEqualTo("BTCUSDT");
        assertThat(llmInput.executiveSummary().headline()).isEqualTo("SHORT_TERM view");
        assertThat(llmInput.executiveSummary().primaryMessage()).isEqualTo("Narrative summary");
        assertThat(llmInput.primaryFacts()).isNotEmpty();
        assertThat(llmInput.primaryFacts()).hasSizeLessThanOrEqualTo(10);
        assertThat(llmInput.domainFactBlocks()).extracting(block -> block.domainType())
                .contains(
                        AnalysisLlmDomainType.MARKET,
                        AnalysisLlmDomainType.DERIVATIVE,
                        AnalysisLlmDomainType.MACRO,
                        AnalysisLlmDomainType.SENTIMENT,
                        AnalysisLlmDomainType.ONCHAIN,
                        AnalysisLlmDomainType.LEVEL,
                        AnalysisLlmDomainType.EXTERNAL
                );
        assertThat(llmInput.crossSignals()).hasSizeLessThanOrEqualTo(5);
        assertThat(llmInput.riskFactors()).hasSizeLessThanOrEqualTo(6);
        assertThat(llmInput.scenarios()).hasSizeLessThanOrEqualTo(3);
        assertThat(llmInput.signalHeadlines()).hasSizeLessThanOrEqualTo(8);
    }
}
