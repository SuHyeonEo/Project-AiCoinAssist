package com.aicoinassist.batch.infrastructure.client.openai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OpenAiChatCompletionRequest(
        String model,
        List<Message> messages,
        ResponseFormat responseFormat
) {

    public record Message(
            String role,
            String content
    ) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ResponseFormat(
            String type,
            JsonSchemaFormat jsonSchema
    ) {
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record JsonSchemaFormat(
            String name,
            boolean strict,
            JsonNode schema
    ) {
    }
}
