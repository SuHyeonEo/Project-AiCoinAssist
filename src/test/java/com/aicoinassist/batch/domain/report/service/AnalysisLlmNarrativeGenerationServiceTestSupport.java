package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmCrossSignalIntegrationOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmDomainAnalysisOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmExecutiveConclusionOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGatewayResponse;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGenerationResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeOutputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmOutputProcessingResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmPromptComposition;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmScenarioOutput;
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
                new AnalysisLlmExecutiveConclusionOutput(
                        "mixed",
                        List.of("support"),
                        List.of("risk"),
                        "Short summary"
                ),
                List.of(
                        new AnalysisLlmDomainAnalysisOutput(
                                "MARKET",
                                "Current signal",
                                List.of("Fact"),
                                "Interpretation",
                                "mixed",
                                "medium",
                                List.of("Caveat")
                        )
                ),
                new AnalysisLlmCrossSignalIntegrationOutput(
                        List.of("Aligned"),
                        List.of("Conflicting"),
                        List.of("Driver"),
                        "Combined structure"
                ),
                List.of(
                        new AnalysisLlmScenarioOutput(
                                "neutral",
                                "Condition",
                                List.of("Trigger"),
                                List.of("Confirm"),
                                List.of("Invalidate"),
                                "Interpretation"
                        )
                ),
                List.of()
        );
        AnalysisLlmPromptComposition composition = promptComposer.compose(input);
        return new AnalysisLlmNarrativeGenerationResult(
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
