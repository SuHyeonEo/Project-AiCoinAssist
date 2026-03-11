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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "external.openai", name = "enabled", havingValue = "true")
public class OpenAiAnalysisLlmNarrativeGateway implements AnalysisLlmNarrativeGateway {

    private static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final OpenAiProperties openAiProperties;

    @Autowired
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
            ResponseEntity<byte[]> responseEntity = restClient.post()
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
                    .exchange((clientRequest, clientResponse) -> toResponseEntity(clientResponse));

            String rawResponse = decodeResponseBody(responseEntity);
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
        } catch (ResourceAccessException exception) {
            throw new AnalysisLlmNarrativeGatewayException(
                    isTimeout(exception) ? AnalysisLlmNarrativeFailureType.TIMEOUT : AnalysisLlmNarrativeFailureType.NETWORK,
                    true,
                    "OpenAI narrative request failed due to network access issue.",
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

    private String decodeResponseBody(ResponseEntity<byte[]> responseEntity) {
        byte[] responseBody = responseEntity.getBody();
        if (responseBody == null || responseBody.length == 0) {
            return null;
        }

        MediaType contentType = responseEntity.getHeaders().getContentType();
        Charset charset = contentType == null || contentType.getCharset() == null
                ? StandardCharsets.UTF_8
                : contentType.getCharset();
        return new String(responseBody, charset);
    }

    private ResponseEntity<byte[]> toResponseEntity(RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse clientResponse)
            throws IOException {
        HttpStatusCode statusCode = clientResponse.getStatusCode();
        byte[] responseBody = clientResponse.getBody().readAllBytes();
        ResponseEntity<byte[]> responseEntity = ResponseEntity.status(statusCode)
                .headers(clientResponse.getHeaders())
                .body(responseBody);

        if (statusCode.is2xxSuccessful()) {
            return responseEntity;
        }

        String responseBodyText = decodeResponseBody(responseEntity);
        String errorSuffix = responseBodyText == null || responseBodyText.isBlank()
                ? ""
                : " Response body: " + responseBodyText;

        if (statusCode.value() == 429) {
            throw new AnalysisLlmNarrativeGatewayException(
                    AnalysisLlmNarrativeFailureType.RATE_LIMIT,
                    true,
                    "OpenAI narrative request was rate limited." + errorSuffix
            );
        }

        if (statusCode.is5xxServerError()) {
            throw new AnalysisLlmNarrativeGatewayException(
                    AnalysisLlmNarrativeFailureType.PROVIDER_ERROR,
                    true,
                    "OpenAI narrative request failed with provider server error. Status: " + statusCode.value() + "." + errorSuffix
            );
        }

        if (statusCode.is4xxClientError()) {
            throw new AnalysisLlmNarrativeGatewayException(
                    AnalysisLlmNarrativeFailureType.PROVIDER_ERROR,
                    false,
                    "OpenAI narrative request was rejected by the provider. Status: " + statusCode.value() + "." + errorSuffix
            );
        }

        throw new AnalysisLlmNarrativeGatewayException(
                AnalysisLlmNarrativeFailureType.NETWORK,
                true,
                "OpenAI narrative request failed with unexpected status: " + statusCode.value() + "." + errorSuffix
        );
    }

    private static SimpleClientHttpRequestFactory requestFactory(OpenAiProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeoutMillis());
        requestFactory.setReadTimeout(properties.readTimeoutMillis());
        return requestFactory;
    }
}
