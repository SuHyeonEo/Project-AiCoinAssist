package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.config.AnalysisLlmNarrativeProperties;
import com.aicoinassist.batch.domain.report.dto.AnalysisGptReportInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmCrossSignalIntegrationOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmDomainAnalysisOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmExecutiveConclusionOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmHeroSummaryOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmMarketStructureBoxOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGatewayResponse;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeGenerationResult;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeOutputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmScenarioOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextDomainReference;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextReference;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextResolution;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmValueLabelBasisOutput;
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
    private AnalysisGptReportInputReadService analysisGptReportInputReadService;

    @Mock
    private AnalysisLlmSharedContextGenerationService analysisLlmSharedContextGenerationService;

    @Mock
    private AnalysisLlmNarrativeGateway analysisLlmNarrativeGateway;

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void generateLatestRetriesTransportFailureAndSucceeds() throws Exception {
        AnalysisLlmNarrativeGenerationService service = service(2);
        AnalysisGptReportInputPayload input = reportInput();
        when(analysisGptReportInputReadService.getLatestInput("BTCUSDT", AnalysisReportType.SHORT_TERM))
                .thenReturn(input);
        when(analysisLlmSharedContextGenerationService.getOrGenerate(input))
                .thenReturn(sharedContextResolution());
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
        assertThat(result.sharedContextResolution()).isNotNull();
        assertThat(result.sharedContextResolution().sharedContextId()).isEqualTo(77L);
        assertThat(result.outputProcessingResult().fallbackUsed()).isFalse();
        verify(analysisLlmNarrativeGateway, times(2)).generate(any());
    }

    @Test
    void generateLatestFallsBackOnContentFailureWithoutRetry() {
        AnalysisLlmNarrativeGenerationService service = service(1);
        AnalysisGptReportInputPayload input = reportInput();
        when(analysisGptReportInputReadService.getLatestInput("BTCUSDT", AnalysisReportType.SHORT_TERM))
                .thenReturn(input);
        when(analysisLlmSharedContextGenerationService.getOrGenerate(input))
                .thenReturn(sharedContextResolution());
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
        assertThat(result.sharedContextResolution()).isNotNull();
        assertThat(result.outputProcessingResult().fallbackUsed()).isTrue();
        verify(analysisLlmNarrativeGateway, times(1)).generate(any());
    }

    @Test
    void generateLatestFallsBackAfterRetryableTransportFailuresExhausted() {
        AnalysisLlmNarrativeGenerationService service = service(2);
        AnalysisGptReportInputPayload input = reportInput();
        when(analysisGptReportInputReadService.getLatestInput("BTCUSDT", AnalysisReportType.SHORT_TERM))
                .thenReturn(input);
        when(analysisLlmSharedContextGenerationService.getOrGenerate(input))
                .thenReturn(sharedContextResolution());
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
        assertThat(result.sharedContextResolution()).isNotNull();
        assertThat(result.outputProcessingResult().fallbackUsed()).isTrue();
        verify(analysisLlmNarrativeGateway, times(2)).generate(any());
    }

    private AnalysisLlmNarrativeGenerationService service(int maxTransportAttempts) {
        return new AnalysisLlmNarrativeGenerationService(
                new AnalysisLlmNarrativeProperties(
                        true,
                        "openai",
                        "llm-prompt-v1",
                        "llm-input-v1",
                        "llm-output-v1",
                        maxTransportAttempts
                ),
                analysisGptReportInputReadService,
                analysisLlmSharedContextGenerationService,
                new AnalysisLlmNarrativeInputAssembler(),
                new AnalysisLlmPromptComposer(objectMapper),
                analysisLlmNarrativeGateway,
                new AnalysisLlmOutputPostProcessor(objectMapper, new AnalysisLlmOutputFallbackFactory()),
                new AnalysisLlmOutputFallbackFactory()
        );
    }

    private AnalysisGptReportInputPayload reportInput() {
        return new AnalysisGptReportInputAssembler(new AnalysisGptCrossSignalFactory())
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
                );
    }

    private AnalysisLlmSharedContextReference sharedContextReference() {
        return new AnalysisLlmSharedContextReference(
                "shared-v1",
                "거시와 심리 공통 맥락은 혼조로 정리됩니다.",
                new AnalysisLlmSharedContextDomainReference(
                        "MIXED",
                        "거시 조합은 뚜렷한 한 방향보다 혼조에 가깝습니다.",
                        "달러와 금리 흐름을 함께 확인할 필요가 있습니다."
                ),
                new AnalysisLlmSharedContextDomainReference(
                        "MIXED",
                        "심리 지표는 아직 확신을 주지 못하고 있습니다.",
                        "심리 개선 여부를 추가로 확인할 필요가 있습니다."
                )
        );
    }

    private AnalysisLlmSharedContextResolution sharedContextResolution() {
        return new AnalysisLlmSharedContextResolution(
                77L,
                "shared-v1",
                sharedContextReference()
        );
    }

    private AnalysisLlmNarrativeOutputPayload validOutput() {
        return new AnalysisLlmNarrativeOutputPayload(
                new AnalysisLlmHeroSummaryOutput("혼조 국면으로 해석됩니다", "핵심 흐름은 제한적 혼조로 정리됩니다", "주요 동인은 구조 혼합으로 보입니다", "주요 리스크는 변동성 재확대 가능성입니다"),
                new AnalysisLlmExecutiveConclusionOutput(
                        "핵심 구조는 아직 제한적 혼조로 해석됩니다.",
                        List.of("support 1", "support 2", "support 3"),
                        List.of("risk 1", "risk 2", "risk 3"),
                        "추가 확인 전까지는 보수적으로 해석할 필요가 있습니다."
                ),
                List.of(
                        new AnalysisLlmDomainAnalysisOutput("MARKET", "MIXED", "시장 구조는 혼조로 해석됩니다.", "가격 위치를 추가로 확인할 필요가 있습니다."),
                        new AnalysisLlmDomainAnalysisOutput("DERIVATIVE", "MIXED", "파생 신호는 아직 일방향으로 해석되지 않습니다.", "펀딩과 OI를 함께 확인할 필요가 있습니다."),
                        new AnalysisLlmDomainAnalysisOutput("MACRO", "MIXED", "거시 압력은 제한적으로 반영되고 있습니다.", "달러와 금리 흐름을 함께 확인할 필요가 있습니다."),
                        new AnalysisLlmDomainAnalysisOutput("SENTIMENT", "MIXED", "심리 지표는 아직 확신을 주지 못하고 있습니다.", "심리 개선 여부를 확인할 필요가 있습니다."),
                        new AnalysisLlmDomainAnalysisOutput("ONCHAIN", "MIXED", "온체인 흐름은 중립적으로 해석됩니다.", "활동 변화 폭을 확인할 필요가 있습니다."),
                        new AnalysisLlmDomainAnalysisOutput("LEVEL", "MIXED", "레벨 구조는 상하단 확인이 모두 필요한 구간으로 해석됩니다.", "주요 지지와 저항을 함께 확인할 필요가 있습니다.")
                ),
                new AnalysisLlmMarketStructureBoxOutput(
                        "65618.49",
                        "70442.18",
                        "73716",
                        new AnalysisLlmValueLabelBasisOutput("약 60%", "레인지 내 포지션", "LAST_7D 포지션 59.57%"),
                        new AnalysisLlmValueLabelBasisOutput("71192.76", "저항 구간", "LAST_7D range high"),
                        new AnalysisLlmValueLabelBasisOutput("69452.42", "지지 구간", "nearest support zone"),
                        new AnalysisLlmValueLabelBasisOutput("100%", "지지 이탈 리스크", "근접 지지선과 현재가 간격이 매우 좁음"),
                        new AnalysisLlmValueLabelBasisOutput("100%", "저항 돌파 리스크", "돌파 시 신규 레인지 형성 가능성"),
                        "현재 가격은 상하단 기준 사이에서 구조 재확인이 필요한 구간으로 해석됩니다."
                ),
                new AnalysisLlmCrossSignalIntegrationOutput(
                        "정렬과 충돌이 함께 나타나고 있습니다.",
                        List.of("Driver 1", "Driver 2"),
                        "시장과 심리 신호가 엇갈리고 있습니다.",
                        "당분간은 보수적 해석을 유지할 필요가 있습니다."
                ),
                List.of(
                        new AnalysisLlmScenarioOutput("BULLISH", "상방 시나리오", "상단 저항이 유지적으로 완화되고 있습니다.", "상단 돌파 신호가 확인됩니다.", "모멘텀 강화가 이어집니다.", "상단 안착이 실패하면 무효화됩니다.", "상방 확장 가능성을 열어두는 해석입니다."),
                        new AnalysisLlmScenarioOutput("BASE", "기본 시나리오", "현재 구조가 유지되고 있습니다.", "레인지 유지가 이어집니다.", "혼조 흐름이 안정적으로 이어집니다.", "레인지 이탈이 나오면 무효화됩니다.", "현재 구조 유지 가능성을 우선 반영하는 해석입니다."),
                        new AnalysisLlmScenarioOutput("BEARISH", "하방 시나리오", "하단 지지 압력이 커지고 있습니다.", "지지 이탈 신호가 확인됩니다.", "외부 부담 확대가 이어집니다.", "지지 회복이 나오면 무효화됩니다.", "하방 압력 확대 가능성을 반영하는 해석입니다.")
                )
        );
    }
}
