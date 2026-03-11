package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLlmCrossSignalIntegrationOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmDomainAnalysisOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmExecutiveConclusionOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmHeroSummaryOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmMarketStructureBoxOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeOutputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmScenarioOutput;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmValueLabelBasisOutput;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisLlmOutputPostProcessorTest extends AnalysisReportPayloadTestFixtures {

    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
    private final AnalysisGptReportInputAssembler gptAssembler =
            new AnalysisGptReportInputAssembler(new AnalysisGptCrossSignalFactory());
    private final AnalysisLlmNarrativeInputAssembler llmAssembler = new AnalysisLlmNarrativeInputAssembler();
    private final AnalysisLlmOutputPostProcessor processor =
            new AnalysisLlmOutputPostProcessor(objectMapper, new AnalysisLlmOutputFallbackFactory());

    @Test
    void processNormalizesLongStructuredOutputWithoutFallback() throws Exception {
        AnalysisLlmNarrativeInputPayload input = llmInput();
        AnalysisLlmNarrativeOutputPayload output = new AnalysisLlmNarrativeOutputPayload(
                new AnalysisLlmHeroSummaryOutput(
                        repeat("regime", 20),
                        repeat("take", 20),
                        repeat("driver", 20),
                        repeat("risk", 20)
                ),
                new AnalysisLlmExecutiveConclusionOutput(
                        repeat("summary", 60),
                        List.of(repeat("support", 30), repeat("support", 30), repeat("support", 30), repeat("support", 30)),
                        List.of(repeat("risk", 30), repeat("risk", 30), repeat("risk", 30), repeat("risk", 30)),
                        repeat("tactical", 40)
                ),
                List.of(
                        new AnalysisLlmDomainAnalysisOutput(
                                "MARKET",
                                repeat("bullish", 10),
                                repeat("interpretation", 40),
                                repeat("watch", 20)
                        ),
                        new AnalysisLlmDomainAnalysisOutput(
                                "DERIVATIVE",
                                repeat("neutral", 8),
                                repeat("derivative", 30),
                                repeat("monitor", 15)
                        ),
                        new AnalysisLlmDomainAnalysisOutput(
                                "MACRO",
                                repeat("neutral", 8),
                                repeat("macro", 30),
                                repeat("monitor", 15)
                        ),
                        new AnalysisLlmDomainAnalysisOutput(
                                "SENTIMENT",
                                repeat("bearish", 8),
                                repeat("sentiment", 30),
                                repeat("monitor", 15)
                        ),
                        new AnalysisLlmDomainAnalysisOutput(
                                "ONCHAIN",
                                repeat("mixed", 8),
                                repeat("onchain", 30),
                                repeat("monitor", 15)
                        ),
                        new AnalysisLlmDomainAnalysisOutput(
                                "LEVEL",
                                repeat("mixed", 8),
                                repeat("level", 30),
                                repeat("monitor", 15)
                        )
                ),
                new AnalysisLlmMarketStructureBoxOutput(
                        repeat("low", 20),
                        repeat("current", 20),
                        repeat("high", 20),
                        new AnalysisLlmValueLabelBasisOutput(repeat("value", 20), repeat("label", 12), repeat("basis", 20)),
                        new AnalysisLlmValueLabelBasisOutput(repeat("value", 20), repeat("label", 12), repeat("basis", 20)),
                        new AnalysisLlmValueLabelBasisOutput(repeat("value", 20), repeat("label", 12), repeat("basis", 20)),
                        new AnalysisLlmValueLabelBasisOutput(repeat("value", 20), repeat("label", 12), repeat("basis", 20)),
                        new AnalysisLlmValueLabelBasisOutput(repeat("value", 20), repeat("label", 12), repeat("basis", 20)),
                        repeat("interpretation", 40)
                ),
                new AnalysisLlmCrossSignalIntegrationOutput(
                        repeat("align", 30),
                        List.of(repeat("driver", 20), repeat("driver", 20), repeat("driver", 20), repeat("driver", 20), repeat("driver", 20)),
                        repeat("conflict", 30),
                        repeat("position", 30)
                ),
                List.of(
                        new AnalysisLlmScenarioOutput(
                                "BULLISH",
                                repeat("title", 20),
                                repeat("condition", 30),
                                repeat("trigger", 30),
                                repeat("confirmation", 30),
                                repeat("invalidation", 30),
                                repeat("interpretation", 40)
                        ),
                        new AnalysisLlmScenarioOutput(
                                "BASE",
                                "base",
                                "condition",
                                "base trigger",
                                "base confirmation",
                                "base invalidation",
                                "base interpretation"
                        ),
                        new AnalysisLlmScenarioOutput(
                                "BEARISH",
                                "bearish",
                                "condition",
                                "bearish trigger",
                                "bearish confirmation",
                                "bearish invalidation",
                                "bearish interpretation"
                        ),
                        new AnalysisLlmScenarioOutput(
                                "BEARISH",
                                "extra",
                                "condition",
                                "trigger",
                                "confirmation",
                                "invalidation",
                                "interpretation"
                        )
                )
        );

        var result = processor.process(input, objectMapper.writeValueAsString(output));

        assertThat(result.fallbackUsed()).isFalse();
        assertThat(result.issues()).isNotEmpty();
        assertThat(result.output().heroSummary().marketRegime().length()).isLessThanOrEqualTo(80);
        assertThat(result.output().executiveConclusion().summary().length()).isLessThanOrEqualTo(240);
        assertThat(result.output().executiveConclusion().bullishFactors()).hasSizeLessThanOrEqualTo(3);
        assertThat(result.output().marketStructureBox().rangeLow()).isEqualTo(input.serverMarketStructure().rangeLow());
        assertThat(result.output().marketStructureBox().currentPrice()).isEqualTo(input.serverMarketStructure().currentPrice());
        assertThat(result.output().marketStructureBox().rangeHigh()).isEqualTo(input.serverMarketStructure().rangeHigh());
        assertThat(result.output().marketStructureBox().rangePosition()).isEqualTo(input.serverMarketStructure().rangePosition());
        assertThat(result.output().marketStructureBox().upsideReference()).isEqualTo(input.serverMarketStructure().upsideReference());
        assertThat(result.output().marketStructureBox().downsideReference()).isEqualTo(input.serverMarketStructure().downsideReference());
        assertThat(result.output().marketStructureBox().supportBreakRisk()).isEqualTo(input.serverMarketStructure().supportBreakRisk());
        assertThat(result.output().marketStructureBox().resistanceBreakRisk()).isEqualTo(input.serverMarketStructure().resistanceBreakRisk());
        assertThat(result.output().marketStructureBox().interpretation().length()).isLessThanOrEqualTo(260);
        assertThat(result.output().scenarioMap()).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    void processFallsBackWhenJsonIsInvalid() {
        var result = processor.process(llmInput(), "{invalid json");

        assertThat(result.fallbackUsed()).isTrue();
        assertThat(result.output().heroSummary()).isNotNull();
        assertThat(result.output().executiveConclusion()).isNotNull();
        assertThat(result.output().domainAnalyses()).isNotEmpty();
    }

    @Test
    void processAcceptsJsonWrappedInMarkdownFence() throws Exception {
        AnalysisLlmNarrativeOutputPayload output = validOutput();

        var result = processor.process(llmInput(), "```json\n" + objectMapper.writeValueAsString(output) + "\n```");

        assertThat(result.fallbackUsed()).isFalse();
        assertThat(result.issues()).contains("Removed markdown fences from LLM output.");
        assertThat(result.output().executiveConclusion().summary()).isEqualTo("핵심 구조는 아직 제한적 혼조로 해석됩니다.");
    }

    @Test
    void processPreservesParsedOutputWhenKoreanNarrativeUsesBluntSentenceEndings() throws Exception {
        AnalysisLlmNarrativeOutputPayload output = new AnalysisLlmNarrativeOutputPayload(
                new AnalysisLlmHeroSummaryOutput(
                        "단기 상승 기조가 유지 중이다",
                        "단기적으로 강세 구조가 유효하다",
                        "모멘텀 개선이 핵심 동인이다",
                        "외부 하방 부담이 주요 리스크다"
                ),
                new AnalysisLlmExecutiveConclusionOutput(
                        "가격 구조가 상승 우위를 보인다",
                        List.of("RSI가 높다", "MACD가 양호하다", "가격 구조가 양호하다"),
                        List.of("심리가 약하다", "외부 부담이 있다", "지지 이탈 위험이 있다"),
                        "상승 기조를 존중하되 외부 리스크를 관찰해야 한다"
                ),
                List.of(
                        new AnalysisLlmDomainAnalysisOutput("MARKET", "BULLISH", "상승 우위 구조가 유지된다", "MA20 위치를 확인해야 한다"),
                        new AnalysisLlmDomainAnalysisOutput("DERIVATIVE", "NEUTRAL", "방향성 확정이 어렵다", "OI와 펀딩을 확인해야 한다"),
                        new AnalysisLlmDomainAnalysisOutput("MACRO", "NEUTRAL", "거시 압력은 제한적이다", "DXY 흐름을 확인해야 한다"),
                        new AnalysisLlmDomainAnalysisOutput("SENTIMENT", "BEARISH", "심리 부담이 남아 있다", "공포 심리 완화를 확인해야 한다"),
                        new AnalysisLlmDomainAnalysisOutput("ONCHAIN", "NEUTRAL", "온체인 신호는 제한적이다", "활동 추세를 확인해야 한다"),
                        new AnalysisLlmDomainAnalysisOutput("LEVEL", "BEARISH", "지지 방어 여부가 중요하다", "지지 이탈 여부를 확인해야 한다")
                ),
                new AnalysisLlmMarketStructureBoxOutput(
                        "65618.49",
                        "70442.18",
                        "73716",
                        new AnalysisLlmValueLabelBasisOutput("46%", "레인지 내 포지션", "LAST_7D 포지션 46%"),
                        new AnalysisLlmValueLabelBasisOutput("71192.76", "저항 구간", "LAST_7D range high"),
                        new AnalysisLlmValueLabelBasisOutput("69452.42", "지지 구간", "nearest support zone"),
                        new AnalysisLlmValueLabelBasisOutput("100%", "지지 이탈 리스크", "근접 지지선과 현재가 간격이 매우 좁음"),
                        new AnalysisLlmValueLabelBasisOutput("100%", "저항 돌파 리스크", "돌파 시 신규 레인지 형성 가능성"),
                        "현재 가격은 범위 중단에서 상하단 기준을 함께 확인하는 구간입니다."
                ),
                new AnalysisLlmCrossSignalIntegrationOutput(
                        "시장과 모멘텀이 같은 방향을 가리킨다",
                        List.of("시장 구조", "모멘텀"),
                        "심리와 레벨이 반대 신호를 준다",
                        "중립적으로 대응해야 한다"
                ),
                List.of(
                        new AnalysisLlmScenarioOutput("BULLISH", "상승 지속", "가격이 MA20 위를 유지한다", "상단 돌파가 나온다", "모멘텀이 강화된다", "MA20 이탈이 나온다", "상승 경로가 이어진다"),
                        new AnalysisLlmScenarioOutput("BASE", "기본 경로", "현재 구조가 유지된다", "횡보가 이어진다", "변동성이 안정된다", "구조가 약화된다", "기존 흐름이 유지된다"),
                        new AnalysisLlmScenarioOutput("BEARISH", "하방 경로", "지지 이탈이 발생한다", "매도 압력이 확대된다", "외부 부담이 커진다", "지지 회복이 나온다", "하방 경로가 열린다")
                )
        );

        var result = processor.process(llmInput(), objectMapper.writeValueAsString(output));

        assertThat(result.fallbackUsed()).isFalse();
        assertThat(result.issues()).anyMatch(issue -> issue.contains("polite Korean report-style endings"));
        assertThat(result.output().heroSummary().marketRegime()).isEqualTo("단기 상승 기조가 유지 중이다");
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
                        new AnalysisLlmValueLabelBasisOutput("46%", "레인지 내 포지션", "LAST_7D 포지션 46%"),
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

    private AnalysisLlmNarrativeInputPayload llmInput() {
        AnalysisReportEntity entity = reportEntity(
                AnalysisReportType.SHORT_TERM,
                Instant.parse("2026-03-09T00:59:59Z"),
                Instant.parse("2026-03-09T00:59:30Z"),
                "snapshotTime=2026-03-09T00:59:59Z;latestCandleOpenTime=2026-03-08T23:59:59Z;priceSourceEventTime=2026-03-09T00:59:30Z",
                "gpt-5.4",
                "{\"summary\":\"unused\"}",
                Instant.parse("2026-03-09T01:00:30Z")
        );
        return llmAssembler.assemble(gptAssembler.assemble(entity, shortTermPayload("Prompt summary")));
    }

    private String repeat(String value, int times) {
        return String.join(" ", java.util.Collections.nCopies(times, value));
    }
}
