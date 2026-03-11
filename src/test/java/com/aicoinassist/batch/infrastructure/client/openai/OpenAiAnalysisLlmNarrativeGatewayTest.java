package com.aicoinassist.batch.infrastructure.client.openai;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGatewayRequest;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGatewayResponse;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
import com.aicoinassist.batch.domain.report.service.AnalysisLlmNarrativeGatewayException;
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

class OpenAiAnalysisLlmNarrativeGatewayTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void generateMapsOpenAiChatCompletionResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        OpenAiAnalysisLlmNarrativeGateway gateway = new OpenAiAnalysisLlmNarrativeGateway(
                restClient,
                objectMapper,
                properties(true)
        );

        server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-openai-key"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.model").value("gpt-5.4"))
                .andExpect(jsonPath("$.messages[0].role").value("system"))
                .andExpect(jsonPath("$.messages[1].role").value("user"))
                .andExpect(content().string(containsString("\"json_schema\"")))
                .andExpect(content().string(containsString("analysis_market_narrative")))
                .andRespond(withSuccess(
                        """
                        {
                          "id": "chatcmpl-123",
                          "model": "gpt-5.4",
                          "choices": [
                            {
                              "message": {
                                "role": "assistant",
                                "content": "{\\"executive_conclusion\\":{},\\"domain_analyses\\":[],\\"cross_signal_integration\\":{},\\"scenario_map\\":[]}"
                              }
                            }
                          ],
                          "usage": {
                            "prompt_tokens": 1234,
                            "completion_tokens": 456,
                            "total_tokens": 1690
                          }
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ).headers(responseHeaders()));

        AnalysisLlmNarrativeGatewayResponse response = gateway.generate(request());

        assertThat(response.providerModel()).isEqualTo("gpt-5.4");
        assertThat(response.providerRequestId()).isEqualTo("req-openai-1");
        assertThat(response.inputTokens()).isEqualTo(1234);
        assertThat(response.outputTokens()).isEqualTo(456);
        assertThat(response.rawOutputJson()).contains("\"executive_conclusion\"");
        server.verify();
    }

    @Test
    void generateMapsRateLimitAsRetryableFailure() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        OpenAiAnalysisLlmNarrativeGateway gateway = new OpenAiAnalysisLlmNarrativeGateway(
                restClient,
                objectMapper,
                properties(true)
        );

        server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
                .andRespond(withTooManyRequests());

        assertThatThrownBy(() -> gateway.generate(request()))
                .isInstanceOf(AnalysisLlmNarrativeGatewayException.class)
                .satisfies(exception -> {
                    AnalysisLlmNarrativeGatewayException gatewayException =
                            (AnalysisLlmNarrativeGatewayException) exception;
                    assertThat(gatewayException.getFailureType()).isEqualTo(AnalysisLlmNarrativeFailureType.RATE_LIMIT);
                    assertThat(gatewayException.isRetryable()).isTrue();
                });
    }

    private AnalysisLlmNarrativeGatewayRequest request() {
        return new AnalysisLlmNarrativeGatewayRequest(
                "system prompt",
                "user prompt",
                "{\"metadata\":{}}",
                "{\"type\":\"object\",\"required\":[\"executive_conclusion\"]}",
                "{\"executive_conclusion_summary_max_chars\":320}"
        );
    }

    private OpenAiProperties properties(boolean enabled) {
        return new OpenAiProperties(
                enabled,
                "https://api.openai.com",
                "test-openai-key",
                "gpt-5.4",
                null,
                null,
                5000,
                30000
        );
    }

    private HttpHeaders responseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-request-id", "req-openai-1");
        return headers;
    }
}
