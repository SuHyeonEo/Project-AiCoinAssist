package com.aicoinassist.batch.domain.news.dto;

public record ReferenceNewsGatewayRequest(
        String systemPrompt,
        String userPrompt,
        String inputPayloadJson,
        String outputSchemaJson,
        String outputLengthPolicyJson
) {

    public static ReferenceNewsGatewayRequest from(ReferenceNewsPromptComposition composition) {
        return new ReferenceNewsGatewayRequest(
                composition.systemPrompt(),
                composition.userPrompt(),
                composition.inputPayloadJson(),
                composition.outputSchemaJson(),
                composition.outputLengthPolicyJson()
        );
    }
}
