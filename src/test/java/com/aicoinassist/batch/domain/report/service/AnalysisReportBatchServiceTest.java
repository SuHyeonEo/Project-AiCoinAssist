package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.service.MarketIndicatorSnapshotPersistenceService;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchProperties;
import com.aicoinassist.batch.domain.report.config.AnalysisLlmNarrativeProperties;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportBatchResult;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportNarrativeEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeFailureType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisLlmNarrativeGenerationStatus;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisReportBatchServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-03-09T01:00:30Z"),
            ZoneOffset.UTC
    );

    @Mock
    private MarketIndicatorSnapshotPersistenceService marketIndicatorSnapshotPersistenceService;

    @Mock
    private AnalysisReportGenerationService analysisReportGenerationService;

    @Mock
    private AnalysisReportNarrativeGenerationFlowService analysisReportNarrativeGenerationFlowService;

    @Test
    void generateForAssetCreatesSnapshotsBeforeReportsForAllHorizons() {
        AnalysisReportBatchProperties properties = new AnalysisReportBatchProperties(
                "report-assembler-v1",
                java.util.List.of(AssetType.BTC),
                java.util.List.of(
                        AnalysisReportType.SHORT_TERM,
                        AnalysisReportType.MID_TERM,
                        AnalysisReportType.LONG_TERM
                ),
                true,
                60000L,
                0L,
                true,
                60000L,
                0L,
                true,
                60000L,
                0L
        );
        AnalysisReportBatchService service = new AnalysisReportBatchService(
                marketIndicatorSnapshotPersistenceService,
                analysisReportGenerationService,
                analysisReportNarrativeGenerationFlowService,
                properties,
                new AnalysisLlmNarrativeProperties(false, "openai", "llm-prompt-v1", "llm-input-v1", "llm-output-v1", 1),
                FIXED_CLOCK
        );

        Instant storedTime = Instant.parse("2026-03-09T01:00:30Z");

        AnalysisReportBatchResult result = service.generateForAsset(
                AssetType.BTC,
                "run-001",
                "report-assembler-v1",
                storedTime,
                properties.reportTypes()
        );

        InOrder inOrder = inOrder(
                marketIndicatorSnapshotPersistenceService,
                analysisReportGenerationService
        );
        inOrder.verify(marketIndicatorSnapshotPersistenceService).createAndSave("BTCUSDT", CandleInterval.ONE_HOUR);
        inOrder.verify(marketIndicatorSnapshotPersistenceService).createAndSave("BTCUSDT", CandleInterval.FOUR_HOUR);
        inOrder.verify(marketIndicatorSnapshotPersistenceService).createAndSave("BTCUSDT", CandleInterval.ONE_DAY);
        inOrder.verify(analysisReportGenerationService).generateAndSave("BTCUSDT", AnalysisReportType.SHORT_TERM, "report-assembler-v1", storedTime);
        inOrder.verify(analysisReportGenerationService).generateAndSave("BTCUSDT", AnalysisReportType.MID_TERM, "report-assembler-v1", storedTime);
        inOrder.verify(analysisReportGenerationService).generateAndSave("BTCUSDT", AnalysisReportType.LONG_TERM, "report-assembler-v1", storedTime);

        assertThat(result.runId()).isEqualTo("run-001");
        assertThat(result.symbol()).isEqualTo("BTCUSDT");
        assertThat(result.startedAt()).isEqualTo(Instant.parse("2026-03-09T01:00:30Z"));
        assertThat(result.finishedAt()).isEqualTo(Instant.parse("2026-03-09T01:00:30Z"));
        assertThat(result.durationMillis()).isZero();
        assertThat(result.snapshotSuccessCount()).isEqualTo(3);
        assertThat(result.reportSuccessCount()).isEqualTo(3);
        assertThat(result.hasFailures()).isFalse();
    }

    @Test
    void generateForAssetUsesConfiguredSubsetOfReportTypesAndIntervals() {
        AnalysisReportBatchProperties properties = new AnalysisReportBatchProperties(
                "report-assembler-v2",
                java.util.List.of(AssetType.ETH),
                java.util.List.of(AnalysisReportType.MID_TERM, AnalysisReportType.LONG_TERM),
                true,
                60000L,
                0L,
                true,
                60000L,
                0L,
                true,
                60000L,
                0L
        );
        AnalysisReportBatchService service = new AnalysisReportBatchService(
                marketIndicatorSnapshotPersistenceService,
                analysisReportGenerationService,
                analysisReportNarrativeGenerationFlowService,
                properties,
                new AnalysisLlmNarrativeProperties(false, "openai", "llm-prompt-v1", "llm-input-v1", "llm-output-v1", 1),
                FIXED_CLOCK
        );

        AnalysisReportBatchResult result = service.generateForAsset(
                AssetType.ETH,
                "run-002",
                "report-assembler-v2",
                Instant.parse("2026-03-09T01:00:30Z"),
                properties.reportTypes()
        );

        InOrder inOrder = inOrder(
                marketIndicatorSnapshotPersistenceService,
                analysisReportGenerationService
        );
        inOrder.verify(marketIndicatorSnapshotPersistenceService).createAndSave("ETHUSDT", CandleInterval.FOUR_HOUR);
        inOrder.verify(marketIndicatorSnapshotPersistenceService).createAndSave("ETHUSDT", CandleInterval.ONE_DAY);
        inOrder.verify(analysisReportGenerationService).generateAndSave(
                "ETHUSDT",
                AnalysisReportType.MID_TERM,
                "report-assembler-v2",
                Instant.parse("2026-03-09T01:00:30Z")
        );
        inOrder.verify(analysisReportGenerationService).generateAndSave(
                "ETHUSDT",
                AnalysisReportType.LONG_TERM,
                "report-assembler-v2",
                Instant.parse("2026-03-09T01:00:30Z")
        );

        assertThat(result.runId()).isEqualTo("run-002");
        assertThat(result.symbol()).isEqualTo("ETHUSDT");
        assertThat(result.snapshotSuccessCount()).isEqualTo(2);
        assertThat(result.reportSuccessCount()).isEqualTo(2);
    }

    @Test
    void generateForAssetCapturesPartialFailuresByStep() {
        AnalysisReportBatchProperties properties = new AnalysisReportBatchProperties(
                "report-assembler-v3",
                java.util.List.of(AssetType.XRP),
                java.util.List.of(
                        AnalysisReportType.SHORT_TERM,
                        AnalysisReportType.MID_TERM
                ),
                true,
                60000L,
                0L,
                true,
                60000L,
                0L,
                true,
                60000L,
                0L
        );
        AnalysisReportBatchService service = new AnalysisReportBatchService(
                marketIndicatorSnapshotPersistenceService,
                analysisReportGenerationService,
                analysisReportNarrativeGenerationFlowService,
                properties,
                new AnalysisLlmNarrativeProperties(false, "openai", "llm-prompt-v1", "llm-input-v1", "llm-output-v1", 1),
                FIXED_CLOCK
        );

        when(marketIndicatorSnapshotPersistenceService.createAndSave("XRPUSDT", CandleInterval.ONE_HOUR))
                .thenReturn(null);
        when(marketIndicatorSnapshotPersistenceService.createAndSave("XRPUSDT", CandleInterval.FOUR_HOUR))
                .thenThrow(new IllegalStateException("4h snapshot failed"));
        when(analysisReportGenerationService.generateAndSave(
                        "XRPUSDT",
                        AnalysisReportType.SHORT_TERM,
                        "report-assembler-v3",
                        Instant.parse("2026-03-09T01:00:30Z")
                ))
                .thenReturn(null);
        when(analysisReportGenerationService.generateAndSave(
                        "XRPUSDT",
                        AnalysisReportType.MID_TERM,
                        "report-assembler-v3",
                        Instant.parse("2026-03-09T01:00:30Z")
                ))
                .thenThrow(new IllegalStateException("mid report failed"));

        AnalysisReportBatchResult result = service.generateForAsset(
                AssetType.XRP,
                "run-003",
                "report-assembler-v3",
                Instant.parse("2026-03-09T01:00:30Z"),
                properties.reportTypes()
        );

        assertThat(result.runId()).isEqualTo("run-003");
        assertThat(result.symbol()).isEqualTo("XRPUSDT");
        assertThat(result.snapshotResults()).hasSize(2);
        assertThat(result.reportResults()).hasSize(2);
        assertThat(result.hasFailures()).isTrue();
        assertThat(result.snapshotResults()).extracting("success").containsExactly(true, false);
        assertThat(result.reportResults()).extracting("success").containsExactly(true, false);
        assertThat(result.snapshotSuccessCount()).isEqualTo(1);
        assertThat(result.snapshotFailureCount()).isEqualTo(1);
        assertThat(result.reportSuccessCount()).isEqualTo(1);
        assertThat(result.reportFailureCount()).isEqualTo(1);
        assertThat(result.snapshotResults().get(1).errorMessage()).contains("4h snapshot failed");
        assertThat(result.reportResults().get(1).errorMessage()).contains("mid report failed");
    }

    @Test
    void generateForAssetCapturesNarrativeFallbackAsPartialFailure() {
        AnalysisReportBatchProperties properties = new AnalysisReportBatchProperties(
                "report-assembler-v4",
                java.util.List.of(AssetType.BTC),
                java.util.List.of(AnalysisReportType.SHORT_TERM),
                true,
                60000L,
                0L,
                true,
                60000L,
                0L,
                true,
                60000L,
                0L
        );
        AnalysisReportBatchService service = new AnalysisReportBatchService(
                marketIndicatorSnapshotPersistenceService,
                analysisReportGenerationService,
                analysisReportNarrativeGenerationFlowService,
                properties,
                new AnalysisLlmNarrativeProperties(true, "openai", "llm-prompt-v1", "llm-input-v1", "llm-output-v1", 1),
                FIXED_CLOCK
        );

        when(analysisReportNarrativeGenerationFlowService.generateAndStoreLatest("BTCUSDT", AnalysisReportType.SHORT_TERM))
                .thenReturn(AnalysisReportNarrativeEntity.builder()
                        .analysisReport(null)
                        .symbol("BTCUSDT")
                        .reportType(AnalysisReportType.SHORT_TERM)
                        .analysisBasisTime(Instant.parse("2026-03-09T00:59:59Z"))
                        .sourceDataVersion("basis-key")
                        .analysisEngineVersion("report-assembler-v4")
                        .llmProvider("openai")
                        .llmModel("gpt-5.4")
                        .promptTemplateVersion("llm-prompt-v1")
                        .inputSchemaVersion("llm-input-v1")
                        .outputSchemaVersion("llm-output-v1")
                        .inputPayloadHash("hash")
                        .inputPayloadJson("{}")
                        .promptSystemText("system")
                        .promptUserText("user")
                        .outputLengthPolicyJson("{}")
                        .referenceNewsJson("[]")
                        .outputJson("{}")
                        .fallbackUsed(true)
                        .generationStatus(AnalysisLlmNarrativeGenerationStatus.FALLBACK)
                        .failureType(AnalysisLlmNarrativeFailureType.CONTENT)
                        .validationIssuesJson("[]")
                        .requestedAt(Instant.parse("2026-03-09T01:00:30Z"))
                        .completedAt(Instant.parse("2026-03-09T01:00:30Z"))
                        .storedAt(Instant.parse("2026-03-09T01:00:30Z"))
                        .build());

        AnalysisReportBatchResult result = service.generateForAsset(
                AssetType.BTC,
                "run-004",
                "report-assembler-v4",
                Instant.parse("2026-03-09T01:00:30Z"),
                properties.reportTypes()
        );

        assertThat(result.reportResults()).hasSize(1);
        assertThat(result.reportResults().get(0).success()).isTrue();
        assertThat(result.reportResults().get(0).narrativeGenerationStatus()).isEqualTo(AnalysisLlmNarrativeGenerationStatus.FALLBACK);
        assertThat(result.reportResults().get(0).narrativeFallbackUsed()).isTrue();
        assertThat(result.reportResults().get(0).narrativeFailureType()).isEqualTo(AnalysisLlmNarrativeFailureType.CONTENT);
        assertThat(result.status()).isEqualTo(com.aicoinassist.batch.domain.report.enumtype.BatchExecutionStatus.PARTIAL_FAILURE);
    }
}
