package com.aicoinassist.batch.domain.report.dto;

public record AnalysisLlmNarrativeGatewayRequest(
        String systemPrompt,
        String userPrompt,
        String inputPayloadJson,
        String outputSchemaJson,
        String outputLengthPolicyJson
) {

    public static AnalysisLlmNarrativeGatewayRequest from(AnalysisLlmPromptComposition composition) {
        return new AnalysisLlmNarrativeGatewayRequest(
                composition.systemPrompt(),
                composition.userPrompt(),
                composition.inputPayloadJson(),
                composition.outputSchemaJson(),
                composition.outputLengthPolicyJson()
        );
    }
}
