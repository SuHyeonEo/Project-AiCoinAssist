package com.aicoinassist.batch.domain.news.dto;

public record ReferenceNewsPromptComposition(
        String systemPrompt,
        String userPrompt,
        String inputPayloadJson,
        String outputSchemaJson,
        String outputLengthPolicyJson
) {
}
