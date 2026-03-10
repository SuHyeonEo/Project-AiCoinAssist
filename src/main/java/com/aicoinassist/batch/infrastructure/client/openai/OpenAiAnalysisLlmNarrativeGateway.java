package com.aicoinassist.batch.infrastructure.client.openai;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGatewayRequest;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGatewayResponse;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
import com.aicoinassist.batch.domain.report.service.AnalysisLlmNarrativeGateway;
import com.aicoinassist.batch.domain.report.service.AnalysisLlmNarrativeGatewayException;
import com.aicoinassist.batch.global.config.OpenAiProperties;
import com.aicoinassist.batch.infrastructure.client.openai.dto.OpenAiChatCompletionRequest;
import com.aicoinassist.batch.infrastructure.client.openai.dto.OpenAiChatCompletionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "external.openai", name = "enabled", havingValue = "true")
public class OpenAiAnalysisLlmNarrativeGateway implements AnalysisLlmNarrativeGateway {

    private static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final OpenAiProperties openAiProperties;

    public OpenAiAnalysisLlmNarrativeGateway(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            OpenAiProperties openAiProperties
    ) {
        this(
                restClientBuilder.requestFactory(requestFactory(openAiProperties)).build(),
                objectMapper,
                openAiProperties
        );
    }

    OpenAiAnalysisLlmNarrativeGateway(
            RestClient restClient,
            ObjectMapper objectMapper,
            OpenAiProperties openAiProperties
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.openAiProperties = openAiProperties;
    }

    @Override
    public AnalysisLlmNarrativeGatewayResponse generate(AnalysisLlmNarrativeGatewayRequest request) {
        try {
            OpenAiChatCompletionRequest payload = toRequestPayload(request);
            ResponseEntity<String> responseEntity = restClient.post()
                    .uri(openAiProperties.baseUrl() + CHAT_COMPLETIONS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        headers.setBearerAuth(openAiProperties.apiKey());
                        if (openAiProperties.organization() != null && !openAiProperties.organization().isBlank()) {
                            headers.add("OpenAI-Organization", openAiProperties.organization());
                        }
                        if (openAiProperties.project() != null && !openAiProperties.project().isBlank()) {
                            headers.add("OpenAI-Project", openAiProperties.project());
                        }
                    })
                    .body(payload)
                    .retrieve()
                    .toEntity(String.class);

            String rawResponse = responseEntity.getBody();
            if (rawResponse == null || rawResponse.isBlank()) {
                throw new AnalysisLlmNarrativeGatewayException(
                        AnalysisLlmNarrativeFailureType.PROVIDER_ERROR,
                        false,
                        "OpenAI chat completions API returned an empty response body."
                );
            }

            OpenAiChatCompletionResponse response = deserialize(rawResponse);
            return new AnalysisLlmNarrativeGatewayResponse(
                    extractOutputJson(response),
                    response.model() == null || response.model().isBlank() ? openAiProperties.model() : response.model(),
                    responseEntity.getHeaders().getFirst("x-request-id"),
                    response.usage() == null ? null : response.usage().promptTokens(),
                    response.usage() == null ? null : response.usage().completionTokens()
            );
        } catch (HttpClientErrorException.TooManyRequests exception) {
            throw new AnalysisLlmNarrativeGatewayException(
                    AnalysisLlmNarrativeFailureType.RATE_LIMIT,
                    true,
                    "OpenAI narrative request was rate limited.",
                    exception
            );
        } catch (HttpServerErrorException exception) {
            throw new AnalysisLlmNarrativeGatewayException(
                    AnalysisLlmNarrativeFailureType.PROVIDER_ERROR,
                    true,
                    "OpenAI narrative request failed with provider server error.",
                    exception
            );
        } catch (HttpClientErrorException exception) {
            throw new AnalysisLlmNarrativeGatewayException(
                    AnalysisLlmNarrativeFailureType.PROVIDER_ERROR,
                    false,
                    "OpenAI narrative request was rejected by the provider.",
                    exception
            );
        } catch (ResourceAccessException exception) {
            throw new AnalysisLlmNarrativeGatewayException(
                    isTimeout(exception) ? AnalysisLlmNarrativeFailureType.TIMEOUT : AnalysisLlmNarrativeFailureType.NETWORK,
                    true,
                    "OpenAI narrative request failed due to network access issue.",
                    exception
            );
        } catch (RestClientResponseException exception) {
            throw new AnalysisLlmNarrativeGatewayException(
                    AnalysisLlmNarrativeFailureType.NETWORK,
                    true,
                    "OpenAI narrative request failed while reading provider response.",
                    exception
            );
        }
    }

    private OpenAiChatCompletionRequest toRequestPayload(AnalysisLlmNarrativeGatewayRequest request) {
        try {
            JsonNode outputSchema = objectMapper.readTree(request.outputSchemaJson());
            return new OpenAiChatCompletionRequest(
                    openAiProperties.model(),
                    List.of(
                            new OpenAiChatCompletionRequest.Message("system", request.systemPrompt()),
                            new OpenAiChatCompletionRequest.Message("user", request.userPrompt())
                    ),
                    new OpenAiChatCompletionRequest.ResponseFormat(
                            "json_schema",
                            new OpenAiChatCompletionRequest.JsonSchemaFormat(
                                    "analysis_market_narrative",
                                    true,
                                    outputSchema
                            )
                    )
            );
        } catch (JsonProcessingException exception) {
            throw new AnalysisLlmNarrativeGatewayException(
                    AnalysisLlmNarrativeFailureType.CONTENT,
                    false,
                    "Failed to parse the configured LLM output schema JSON.",
                    exception
            );
        }
    }

    private OpenAiChatCompletionResponse deserialize(String rawResponse) {
        try {
            return objectMapper.readValue(rawResponse, OpenAiChatCompletionResponse.class);
        } catch (JsonProcessingException exception) {
            throw new AnalysisLlmNarrativeGatewayException(
                    AnalysisLlmNarrativeFailureType.CONTENT,
                    false,
                    "Failed to deserialize OpenAI narrative response.",
                    exception
            );
        }
    }

    private String extractOutputJson(OpenAiChatCompletionResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new AnalysisLlmNarrativeGatewayException(
                    AnalysisLlmNarrativeFailureType.CONTENT,
                    false,
                    "OpenAI narrative response did not contain a completion choice."
            );
        }

        OpenAiChatCompletionResponse.Message message = response.choices().get(0).message();
        if (message == null || message.content() == null || message.content().isBlank()) {
            throw new AnalysisLlmNarrativeGatewayException(
                    AnalysisLlmNarrativeFailureType.CONTENT,
                    false,
                    "OpenAI narrative response did not contain usable JSON content."
            );
        }

        return message.content();
    }

    private boolean isTimeout(ResourceAccessException exception) {
        Throwable cursor = exception;
        while (cursor != null) {
            if (cursor instanceof SocketTimeoutException) {
                return true;
            }
            cursor = cursor.getCause();
        }
        String message = exception.getMessage();
        return message != null && message.toLowerCase().contains("timed out");
    }

    private static SimpleClientHttpRequestFactory requestFactory(OpenAiProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeoutMillis());
        requestFactory.setReadTimeout(properties.readTimeoutMillis());
        return requestFactory;
    }
}
