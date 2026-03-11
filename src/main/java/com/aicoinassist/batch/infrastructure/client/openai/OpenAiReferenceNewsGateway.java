package com.aicoinassist.batch.infrastructure.client.openai;

import com.aicoinassist.batch.domain.news.dto.ReferenceNewsGatewayRequest;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsGatewayResponse;
import com.aicoinassist.batch.domain.news.enumtype.ReferenceNewsGenerationFailureType;
import com.aicoinassist.batch.domain.news.service.ReferenceNewsGateway;
import com.aicoinassist.batch.domain.news.service.ReferenceNewsGatewayException;
import com.aicoinassist.batch.global.config.OpenAiProperties;
import com.aicoinassist.batch.infrastructure.client.openai.dto.OpenAiResponsesRequest;
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
public class OpenAiReferenceNewsGateway implements ReferenceNewsGateway {

    private static final String RESPONSES_PATH = "/v1/responses";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final OpenAiProperties openAiProperties;

    @Autowired
    public OpenAiReferenceNewsGateway(
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

    OpenAiReferenceNewsGateway(
            RestClient restClient,
            ObjectMapper objectMapper,
            OpenAiProperties openAiProperties
    ) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.openAiProperties = openAiProperties;
    }

    @Override
    public ReferenceNewsGatewayResponse generate(ReferenceNewsGatewayRequest request) {
        try {
            OpenAiResponsesRequest payload = toRequestPayload(request);
            ResponseEntity<byte[]> responseEntity = restClient.post()
                    .uri(openAiProperties.baseUrl() + RESPONSES_PATH)
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
                throw new ReferenceNewsGatewayException(
                        ReferenceNewsGenerationFailureType.PROVIDER_ERROR,
                        false,
                        "OpenAI reference news API returned an empty response body."
                );
            }

            JsonNode responseJson = deserialize(rawResponse);
            return new ReferenceNewsGatewayResponse(
                    extractOutputJson(responseJson),
                    extractModel(responseJson),
                    responseEntity.getHeaders().getFirst("x-request-id"),
                    intValue(responseJson.at("/usage/input_tokens")),
                    intValue(responseJson.at("/usage/output_tokens"))
            );
        } catch (ResourceAccessException exception) {
            throw new ReferenceNewsGatewayException(
                    isTimeout(exception) ? ReferenceNewsGenerationFailureType.TIMEOUT : ReferenceNewsGenerationFailureType.NETWORK,
                    true,
                    "OpenAI reference news request failed due to network access issue.",
                    exception
            );
        }
    }

    private OpenAiResponsesRequest toRequestPayload(ReferenceNewsGatewayRequest request) {
        return new OpenAiResponsesRequest(
                openAiProperties.model(),
                combinedInput(request),
                "auto",
                List.of(new OpenAiResponsesRequest.Tool("web_search", true))
        );
    }

    private String combinedInput(ReferenceNewsGatewayRequest request) {
        return """
                [System Prompt]
                %s

                [User Prompt]
                %s

                [Input Payload JSON]
                %s

                [Output Schema JSON]
                %s

                [Output Length Policy JSON]
                %s
                """.formatted(
                request.systemPrompt(),
                request.userPrompt(),
                request.inputPayloadJson(),
                request.outputSchemaJson(),
                request.outputLengthPolicyJson()
        );
    }

    private JsonNode deserialize(String rawResponse) {
        try {
            return objectMapper.readTree(rawResponse);
        } catch (IOException exception) {
            throw new ReferenceNewsGatewayException(
                    ReferenceNewsGenerationFailureType.CONTENT,
                    false,
                    "Failed to deserialize OpenAI reference news response.",
                    exception
            );
        }
    }

    private String extractOutputJson(JsonNode responseJson) {
        JsonNode outputNode = responseJson.path("output");
        if (!outputNode.isArray() || outputNode.isEmpty()) {
            throw contentError("OpenAI reference news response did not contain output items.");
        }

        StringBuilder builder = new StringBuilder();
        for (JsonNode outputItem : outputNode) {
            if (!"message".equals(outputItem.path("type").asText())) {
                continue;
            }

            JsonNode contentNode = outputItem.path("content");
            if (!contentNode.isArray()) {
                continue;
            }

            for (JsonNode contentItem : contentNode) {
                JsonNode textNode = contentItem.path("text");
                if (textNode.isTextual()) {
                    if (!builder.isEmpty()) {
                        builder.append('\n');
                    }
                    builder.append(textNode.asText());
                }
            }
        }

        if (builder.isEmpty()) {
            throw contentError("OpenAI reference news response did not contain usable text output.");
        }
        return builder.toString();
    }

    private String extractModel(JsonNode responseJson) {
        String model = responseJson.path("model").asText(null);
        return model == null || model.isBlank() ? openAiProperties.model() : model;
    }

    private Integer intValue(JsonNode node) {
        return node.isNumber() ? node.asInt() : null;
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
            throw new ReferenceNewsGatewayException(
                    ReferenceNewsGenerationFailureType.RATE_LIMIT,
                    true,
                    "OpenAI reference news request was rate limited." + errorSuffix
            );
        }

        if (statusCode.is5xxServerError()) {
            throw new ReferenceNewsGatewayException(
                    ReferenceNewsGenerationFailureType.PROVIDER_ERROR,
                    true,
                    "OpenAI reference news request failed with provider server error. Status: " + statusCode.value() + "." + errorSuffix
            );
        }

        if (statusCode.is4xxClientError()) {
            throw new ReferenceNewsGatewayException(
                    ReferenceNewsGenerationFailureType.PROVIDER_ERROR,
                    false,
                    "OpenAI reference news request was rejected by the provider. Status: " + statusCode.value() + "." + errorSuffix
            );
        }

        throw new ReferenceNewsGatewayException(
                ReferenceNewsGenerationFailureType.NETWORK,
                true,
                "OpenAI reference news request failed with unexpected status: " + statusCode.value() + "." + errorSuffix
        );
    }

    private ReferenceNewsGatewayException contentError(String message) {
        return new ReferenceNewsGatewayException(
                ReferenceNewsGenerationFailureType.CONTENT,
                false,
                message
        );
    }

    private static SimpleClientHttpRequestFactory requestFactory(OpenAiProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeoutMillis());
        requestFactory.setReadTimeout(properties.readTimeoutMillis());
        return requestFactory;
    }
}
