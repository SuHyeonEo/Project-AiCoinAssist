package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisGptReportInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmNarrativeInputPayload;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisLlmNarrativeInputReadServiceTest extends AnalysisReportPayloadTestFixtures {

    @Mock
    private AnalysisGptReportInputReadService analysisGptReportInputReadService;

    @Test
    void getLatestInputBuildsLlmNarrativeSchemaFromLatestGptInput() {
        AnalysisLlmNarrativeInputReadService service = new AnalysisLlmNarrativeInputReadService(
                analysisGptReportInputReadService,
                new AnalysisLlmNarrativeInputAssembler()
        );
        AnalysisGptReportInputPayload input = new AnalysisGptReportInputAssembler(new AnalysisGptCrossSignalFactory())
                .assemble(
                        reportEntity(
                                AnalysisReportType.SHORT_TERM,
                                java.time.Instant.parse("2026-03-09T00:59:59Z"),
                                java.time.Instant.parse("2026-03-09T00:59:30Z"),
                                "snapshotTime=2026-03-09T00:59:59Z;latestCandleOpenTime=2026-03-08T23:59:59Z;priceSourceEventTime=2026-03-09T00:59:30Z",
                                "gpt-5.4",
                                "{\"summary\":\"unused\"}",
                                java.time.Instant.parse("2026-03-09T01:00:30Z")
                        ),
                        shortTermPayload("Narrative summary")
                );

        when(analysisGptReportInputReadService.getLatestInput("BTCUSDT", AnalysisReportType.SHORT_TERM))
                .thenReturn(input);

        AnalysisLlmNarrativeInputPayload result = service.getLatestInput("BTCUSDT", AnalysisReportType.SHORT_TERM);

        assertThat(result.executiveSummary().primaryMessage()).isEqualTo("Narrative summary");
        assertThat(result.domainFactBlocks()).isNotEmpty();
        assertThat(result.crossSignals()).isNotEmpty();
    }
}
