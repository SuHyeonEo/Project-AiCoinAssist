package com.aicoinassist.batch.infrastructure.client.openai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenAiResponsesRequest(
        String model,
        String input,
        @JsonProperty("tool_choice")
        String toolChoice,
        List<Tool> tools
) {

    public record Tool(
            String type,
            @JsonProperty("external_web_access")
            boolean externalWebAccess
    ) {
    }
}
