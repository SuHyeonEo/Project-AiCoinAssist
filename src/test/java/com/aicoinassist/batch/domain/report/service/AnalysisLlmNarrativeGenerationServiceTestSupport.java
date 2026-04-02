package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmCrossSignalIntegrationOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmDomainAnalysisOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmExecutiveConclusionOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmHeroSummaryOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmMarketStructureBoxOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGatewayResponse;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGenerationResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeOutputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmOutputProcessingResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmPromptComposition;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmScenarioOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextResolution;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmValueLabelBasisOutput;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

class AnalysisLlmNarrativeGenerationServiceTestSupport {

    private final ObjectMapper objectMapper;

    AnalysisLlmNarrativeGenerationServiceTestSupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    AnalysisLlmNarrativeGenerationResult successfulGenerationResult(AnalysisLlmNarrativeInputPayload input) throws Exception {
        AnalysisLlmPromptComposer promptComposer = new AnalysisLlmPromptComposer(objectMapper);
        AnalysisLlmNarrativeOutputPayload output = new AnalysisLlmNarrativeOutputPayload(
                new AnalysisLlmHeroSummaryOutput("Mixed regime", "One line take", "Primary driver", "Risk driver"),
                new AnalysisLlmExecutiveConclusionOutput(
                        "Short summary",
                        List.of("support 1", "support 2", "support 3"),
                        List.of("risk 1", "risk 2", "risk 3"),
                        "Tactical view"
                ),
                List.of(
                        new AnalysisLlmDomainAnalysisOutput("MARKET", "MIXED", "Interpretation", "Watch point"),
                        new AnalysisLlmDomainAnalysisOutput("DERIVATIVE", "MIXED", "Interpretation", "Watch point"),
                        new AnalysisLlmDomainAnalysisOutput("MACRO", "MIXED", "Interpretation", "Watch point"),
                        new AnalysisLlmDomainAnalysisOutput("SENTIMENT", "MIXED", "Interpretation", "Watch point"),
                        new AnalysisLlmDomainAnalysisOutput("ONCHAIN", "MIXED", "Interpretation", "Watch point"),
                        new AnalysisLlmDomainAnalysisOutput("LEVEL", "MIXED", "Interpretation", "Watch point")
                ),
                new AnalysisLlmMarketStructureBoxOutput(
                        "65618.49",
                        "70442.18",
                        "73716",
                        new AnalysisLlmValueLabelBasisOutput("약 60%", "레인지 내 포지션", "LAST_7D 포지션 59.57%"),
                        new AnalysisLlmValueLabelBasisOutput("71192.76", "저항 구간", "LAST_7D range high"),
                        new AnalysisLlmValueLabelBasisOutput("69452.42", "지지 구간", "nearest support zone"),
                        new AnalysisLlmValueLabelBasisOutput("100%", "지지 이탈 리스크", "근접 지지선과 현재가 간격이 매우 좁음"),
                        new AnalysisLlmValueLabelBasisOutput("100%", "저항 돌파 리스크", "돌파 시 신규 레인지 형성 가능성"),
                        "현재 가격은 상하단 기준 사이에서 구조 재확인이 필요한 구간으로 해석됩니다."
                ),
                new AnalysisLlmCrossSignalIntegrationOutput(
                        "Alignment summary",
                        List.of("Driver 1", "Driver 2"),
                        "Conflict summary",
                        "Positioning take"
                ),
                List.of(
                        new AnalysisLlmScenarioOutput("BULLISH", "Bullish title", "Condition", "Trigger", "Confirmation", "Invalidation", "Interpretation"),
                        new AnalysisLlmScenarioOutput("BASE", "Base title", "Condition", "Trigger", "Confirmation", "Invalidation", "Interpretation"),
                        new AnalysisLlmScenarioOutput("BEARISH", "Bearish title", "Condition", "Trigger", "Confirmation", "Invalidation", "Interpretation")
                )
        );
        AnalysisLlmPromptComposition composition = promptComposer.compose(input);
        return new AnalysisLlmNarrativeGenerationResult(
                input.sharedContextReference() == null
                        ? null
                        : new AnalysisLlmSharedContextResolution(
                        1L,
                        input.sharedContextReference().contextVersion(),
                        input.sharedContextReference()
                ),
                composition,
                new AnalysisLlmNarrativeGatewayResponse(
                        objectMapper.writeValueAsString(output),
                        "gpt-5.4",
                        "req-1",
                        1200,
                        700
                ),
                new AnalysisLlmOutputProcessingResult(output, false, List.of()),
                1,
                false,
                AnalysisLlmNarrativeFailureType.NONE,
                List.of()
        );
    }
}
