package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmCrossSignalIntegrationOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmDomainAnalysisOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmExecutiveConclusionOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGatewayResponse;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGenerationResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeOutputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmScenarioOutput;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisLlmNarrativeGenerationServiceTest extends AnalysisReportPayloadTestFixtures {

    @Mock
    private AnalysisLlmNarrativeInputReadService analysisLlmNarrativeInputReadService;

    @Mock
    private AnalysisLlmNarrativeGateway analysisLlmNarrativeGateway;

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void generateLatestRetriesTransportFailureAndSucceeds() throws Exception {
        AnalysisLlmNarrativeGenerationService service = service();
        AnalysisLlmNarrativeInputPayload input = llmInput();
        when(analysisLlmNarrativeInputReadService.getLatestInput("BTCUSDT", AnalysisReportType.SHORT_TERM))
                .thenReturn(input);
        when(analysisLlmNarrativeGateway.generate(any()))
                .thenThrow(new AnalysisLlmNarrativeGatewayException(
                        AnalysisLlmNarrativeFailureType.TIMEOUT,
                        true,
                        "Provider timeout"
                ))
                .thenReturn(new AnalysisLlmNarrativeGatewayResponse(
                        objectMapper.writeValueAsString(validOutput()),
                        "gpt-5.4",
                        "req-1",
                        1200,
                        700
                ));

        AnalysisLlmNarrativeGenerationResult result = service.generateLatest("BTCUSDT", AnalysisReportType.SHORT_TERM);

        assertThat(result.degraded()).isFalse();
        assertThat(result.failureType()).isEqualTo(AnalysisLlmNarrativeFailureType.NONE);
        assertThat(result.attempts()).isEqualTo(2);
        assertThat(result.transportIssues()).contains("Provider timeout");
        assertThat(result.gatewayResponse()).isNotNull();
        assertThat(result.outputProcessingResult().fallbackUsed()).isFalse();
        verify(analysisLlmNarrativeGateway, times(2)).generate(any());
    }

    @Test
    void generateLatestFallsBackOnContentFailureWithoutRetry() {
        AnalysisLlmNarrativeGenerationService service = service();
        AnalysisLlmNarrativeInputPayload input = llmInput();
        when(analysisLlmNarrativeInputReadService.getLatestInput("BTCUSDT", AnalysisReportType.SHORT_TERM))
                .thenReturn(input);
        when(analysisLlmNarrativeGateway.generate(any()))
                .thenReturn(new AnalysisLlmNarrativeGatewayResponse(
                        "{invalid json",
                        "gpt-5.4",
                        "req-2",
                        1200,
                        700
                ));

        AnalysisLlmNarrativeGenerationResult result = service.generateLatest("BTCUSDT", AnalysisReportType.SHORT_TERM);

        assertThat(result.degraded()).isTrue();
        assertThat(result.failureType()).isEqualTo(AnalysisLlmNarrativeFailureType.CONTENT);
        assertThat(result.attempts()).isEqualTo(1);
        assertThat(result.outputProcessingResult().fallbackUsed()).isTrue();
        verify(analysisLlmNarrativeGateway, times(1)).generate(any());
    }

    @Test
    void generateLatestFallsBackAfterRetryableTransportFailuresExhausted() {
        AnalysisLlmNarrativeGenerationService service = service();
        AnalysisLlmNarrativeInputPayload input = llmInput();
        when(analysisLlmNarrativeInputReadService.getLatestInput("BTCUSDT", AnalysisReportType.SHORT_TERM))
                .thenReturn(input);
        when(analysisLlmNarrativeGateway.generate(any()))
                .thenThrow(new AnalysisLlmNarrativeGatewayException(
                        AnalysisLlmNarrativeFailureType.RATE_LIMIT,
                        true,
                        "Rate limited"
                ));

        AnalysisLlmNarrativeGenerationResult result = service.generateLatest("BTCUSDT", AnalysisReportType.SHORT_TERM);

        assertThat(result.degraded()).isTrue();
        assertThat(result.failureType()).isEqualTo(AnalysisLlmNarrativeFailureType.RATE_LIMIT);
        assertThat(result.attempts()).isEqualTo(2);
        assertThat(result.transportIssues()).containsExactly("Rate limited", "Rate limited");
        assertThat(result.gatewayResponse()).isNull();
        assertThat(result.outputProcessingResult().fallbackUsed()).isTrue();
        verify(analysisLlmNarrativeGateway, times(2)).generate(any());
    }

    private AnalysisLlmNarrativeGenerationService service() {
        return new AnalysisLlmNarrativeGenerationService(
                analysisLlmNarrativeInputReadService,
                new AnalysisLlmPromptComposer(objectMapper),
                analysisLlmNarrativeGateway,
                new AnalysisLlmOutputPostProcessor(objectMapper, new AnalysisLlmOutputFallbackFactory()),
                new AnalysisLlmOutputFallbackFactory()
        );
    }

    private AnalysisLlmNarrativeInputPayload llmInput() {
        return new AnalysisLlmNarrativeInputAssembler().assemble(
                new AnalysisGptReportInputAssembler(new AnalysisGptCrossSignalFactory())
                        .assemble(
                                reportEntity(
                                        AnalysisReportType.SHORT_TERM,
                                        Instant.parse("2026-03-09T00:59:59Z"),
                                        Instant.parse("2026-03-09T00:59:30Z"),
                                        "snapshotTime=2026-03-09T00:59:59Z;latestCandleOpenTime=2026-03-08T23:59:59Z;priceSourceEventTime=2026-03-09T00:59:30Z",
                                        "gpt-5.4",
                                        "{\"summary\":\"unused\"}",
                                        Instant.parse("2026-03-09T01:00:30Z")
                                ),
                                shortTermPayload("Narrative summary")
                        )
        );
    }

    private AnalysisLlmNarrativeOutputPayload validOutput() {
        return new AnalysisLlmNarrativeOutputPayload(
                new AnalysisLlmExecutiveConclusionOutput(
                        "mixed",
                        List.of("support"),
                        List.of("risk"),
                        "Short summary"
                ),
                List.of(
                        new AnalysisLlmDomainAnalysisOutput(
                                "MARKET",
                                "Current signal",
                                List.of("Fact"),
                                "Interpretation",
                                "mixed",
                                "medium",
                                List.of("Caveat")
                        )
                ),
                new AnalysisLlmCrossSignalIntegrationOutput(
                        List.of("Aligned"),
                        List.of("Conflicting"),
                        List.of("Driver"),
                        "Combined structure"
                ),
                List.of(
                        new AnalysisLlmScenarioOutput(
                                "neutral",
                                "Condition",
                                List.of("Trigger"),
                                List.of("Confirm"),
                                List.of("Invalidate"),
                                "Interpretation"
                        )
                ),
                List.of()
        );
    }
}
