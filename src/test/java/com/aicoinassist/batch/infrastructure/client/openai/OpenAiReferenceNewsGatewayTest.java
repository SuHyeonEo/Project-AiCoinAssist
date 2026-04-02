package com.aicoinassist.batch.infrastructure.client.openai;

import com.aicoinassist.batch.domain.news.dto.ReferenceNewsGatewayRequest;
import com.aicoinassist.batch.domain.news.dto.ReferenceNewsGatewayResponse;
import com.aicoinassist.batch.domain.news.enumtype.ReferenceNewsGenerationFailureType;
import com.aicoinassist.batch.domain.news.service.ReferenceNewsGatewayException;
import com.aicoinassist.batch.global.config.OpenAiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withTooManyRequests;

class OpenAiReferenceNewsGatewayTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void generateMapsOpenAiResponsesApiWebSearchResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        OpenAiReferenceNewsGateway gateway = new OpenAiReferenceNewsGateway(
                restClient,
                objectMapper,
                properties(true)
        );

        server.expect(requestTo("https://api.openai.com/v1/responses"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-openai-key"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.model").value("gpt-5-mini"))
                .andExpect(jsonPath("$.tool_choice").value("auto"))
                .andExpect(jsonPath("$.tools[0].type").value("web_search"))
                .andExpect(jsonPath("$.tools[0].external_web_access").value(true))
                .andExpect(content().string(containsString("[Input Payload JSON]")))
                .andRespond(withSuccess(
                        """
                        {
                          "id": "resp_123",
                          "model": "gpt-5-mini",
                          "usage": {
                            "input_tokens": 321,
                            "output_tokens": 210
                          },
                          "output": [
                            {
                              "id": "ws_1",
                              "type": "web_search_call",
                              "status": "completed"
                            },
                            {
                              "id": "msg_1",
                              "type": "message",
                              "content": [
                                {
                                  "type": "output_text",
                                  "text": "{\\"summary\\":\\"daily shared news\\",\\"items\\":[]}",
                                  "annotations": []
                                }
                              ]
                            }
                          ]
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ).headers(responseHeaders()));

        ReferenceNewsGatewayResponse response = gateway.generate(request());

        assertThat(response.model()).isEqualTo("gpt-5-mini");
        assertThat(response.providerRequestId()).isEqualTo("req-openai-news-1");
        assertThat(response.inputTokens()).isEqualTo(321);
        assertThat(response.outputTokens()).isEqualTo(210);
        assertThat(response.rawOutputJson()).contains("\"summary\"");
        server.verify();
    }

    @Test
    void generateMapsRateLimitAsRetryableFailure() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        OpenAiReferenceNewsGateway gateway = new OpenAiReferenceNewsGateway(
                restClient,
                objectMapper,
                properties(true)
        );

        server.expect(requestTo("https://api.openai.com/v1/responses"))
                .andRespond(withTooManyRequests());

        assertThatThrownBy(() -> gateway.generate(request()))
                .isInstanceOf(ReferenceNewsGatewayException.class)
                .satisfies(exception -> {
                    ReferenceNewsGatewayException gatewayException = (ReferenceNewsGatewayException) exception;
                    assertThat(gatewayException.getFailureType()).isEqualTo(ReferenceNewsGenerationFailureType.RATE_LIMIT);
                    assertThat(gatewayException.isRetryable()).isTrue();
                });
    }

    private ReferenceNewsGatewayRequest request() {
        return new ReferenceNewsGatewayRequest(
                "system prompt",
                "user prompt",
                "{\"scope\":\"GLOBAL_CRYPTO\"}",
                "{\"type\":\"object\"}",
                "{\"maxItems\":5}"
        );
    }

    private OpenAiProperties properties(boolean enabled) {
        return new OpenAiProperties(
                enabled,
                "https://api.openai.com",
                "test-openai-key",
                "gpt-5-mini",
                null,
                null,
                5000,
                30000
        );
    }

    private HttpHeaders responseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-request-id", "req-openai-news-1");
        return headers;
    }
}
