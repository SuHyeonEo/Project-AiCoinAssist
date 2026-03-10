package com.aicoinassist.batch.domain.report.dto;

public record AnalysisLlmPromptComposition(
        String systemPrompt,
        String userPrompt,
        String inputPayloadJson,
        String outputSchemaJson,
        String outputLengthPolicyJson
) {
}
