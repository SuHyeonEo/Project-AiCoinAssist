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
        assertThat(llmInput.marketStructureFacts()).isNotEmpty();
        assertThat(llmInput.marketStructureFacts()).anySatisfy(fact ->
                assertThat(fact).containsAnyOf("이동평균", "RSI14", "MACD", "현재 가격"));
        assertThat(llmInput.derivativeStructureFacts()).isNotEmpty();
        assertThat(llmInput.derivativeStructureFacts()).anySatisfy(fact ->
                assertThat(fact).containsAnyOf("펀딩", "OI", "basis", "동행"));
        assertThat(llmInput.macroStructureFacts()).isNotEmpty();
        assertThat(llmInput.macroStructureFacts()).anySatisfy(fact ->
                assertThat(fact).containsAnyOf("DXY", "US10Y", "USD/KRW", "거시"));
        assertThat(llmInput.sentimentStructureFacts()).isNotEmpty();
        assertThat(llmInput.sentimentStructureFacts()).anySatisfy(fact ->
                assertThat(fact).containsAnyOf("Fear", "심리", "greed", "fear"));
        assertThat(llmInput.onchainStructureFacts()).isNotEmpty();
        assertThat(llmInput.onchainStructureFacts()).anySatisfy(fact ->
                assertThat(fact).containsAnyOf("활성 주소", "트랜잭션", "시가총액", "온체인"));
        assertThat(llmInput.externalStructureFacts()).isNotEmpty();
        assertThat(llmInput.externalStructureFacts()).anySatisfy(fact ->
                assertThat(fact).containsAnyOf("외부", "risk", "signal count", "반전 위험"));
        assertThat(llmInput.levelStructureFacts()).isNotEmpty();
        assertThat(llmInput.levelStructureFacts()).anySatisfy(fact ->
                assertThat(fact).containsAnyOf("구간", "거리", "지지", "저항"));
        assertThat(llmInput.serverMarketStructure()).isNotNull();
        assertThat(llmInput.serverMarketStructure().rangePosition().label()).isEqualTo("레인지 내 위치");
        assertThat(llmInput.marketStructureBoxFacts()).isNotEmpty();
        assertThat(llmInput.marketStructureBoxFacts()).anySatisfy(fact ->
                assertThat(fact).containsAnyOf(
                        "range_position_basis:",
                        "upside_reference_basis:",
                        "downside_reference_basis:",
                        "support_break_risk_basis:",
                        "resistance_break_risk_basis:"
                ));
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
        assertThat(llmInput.domainFactBlocks())
                .anySatisfy(block -> {
                    assertThat(block.headline()).isEqualTo("시장 구조");
                    assertThat(block.summary()).contains("현재 가격은");
                    assertThat(block.summary()).contains("추세는");
                    assertThat(block.summary()).contains("변동성은");
                    assertThat(block.summary()).contains("범위 내 위치는");
                });
        assertThat(llmInput.domainFactBlocks())
                .anySatisfy(block -> assertThat(block.headline()).isEqualTo("파생 맥락"));
        assertThat(llmInput.domainFactBlocks())
                .anySatisfy(block -> assertThat(block.headline()).isEqualTo("외부 종합 맥락"));
        assertThat(llmInput.crossSignals()).hasSizeLessThanOrEqualTo(5);
        assertThat(llmInput.scenarioGuidance()).hasSizeLessThanOrEqualTo(3);
        assertThat(llmInput.scenarioGuidance()).allSatisfy(guidance ->
                assertThat(guidance.confirmationFacts()).isNotEmpty());
        assertThat(llmInput.riskFactors()).hasSizeLessThanOrEqualTo(6);
        assertThat(llmInput.scenarios()).hasSizeLessThanOrEqualTo(3);
        assertThat(llmInput.signalHeadlines()).hasSizeLessThanOrEqualTo(8);
    }
}
