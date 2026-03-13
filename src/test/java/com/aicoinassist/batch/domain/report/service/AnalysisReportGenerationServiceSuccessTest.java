package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.dto.MarketIndicatorSnapshotContext;
import com.aicoinassist.batch.domain.market.entity.MarketContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketExternalContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketLevelContextSnapshotEntity;
import com.aicoinassist.batch.domain.macro.entity.MacroContextSnapshotEntity;
import com.aicoinassist.batch.domain.onchain.entity.OnchainFactSnapshotEntity;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextCompositePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimePersistence;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimeStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimeTransition;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceLevel;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceZone;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportDraft;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeDirection;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeSeverity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeTransitionType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisReportGenerationServiceSuccessTest extends AnalysisReportGenerationServiceTestSupport {

    @Test
    void generateAndSaveBuildsDraftFromLatestMappedSnapshot() {
        AnalysisReportGenerationService service = createService();
        MarketIndicatorSnapshotEntity snapshot = snapshot("4h");
        MarketIndicatorSnapshotContext snapshotContext = new MarketIndicatorSnapshotContext(snapshot, List.of());
        AnalysisReportPayload payload = midTermPayload();
        MarketContextSnapshotEntity contextSnapshot = marketContextSnapshotEntity();
        MarketLevelContextSnapshotEntity levelContextSnapshot = levelContextSnapshotEntity();
        MacroContextSnapshotEntity macroContextSnapshot = generationMacroContextSnapshot();
        SentimentSnapshotEntity sentimentSnapshot = generationSentimentSnapshot();
        OnchainFactSnapshotEntity onchainSnapshot = generationOnchainSnapshot();
        MarketExternalContextSnapshotEntity externalContextSnapshot = externalContextSnapshotEntity();
        AnalysisReportEntity savedEntity = savedReportEntity(snapshot);
        AnalysisDerivativeContext derivativeContext = generationDerivativeContextInput();
        AnalysisMacroContext macroContext = payload.macroContext();
        AnalysisSentimentContext sentimentContext = payload.sentimentContext();
        AnalysisOnchainContext onchainContext = payload.onchainContext();
        AnalysisExternalContextWindowSummary externalWindowSummary = new AnalysisExternalContextWindowSummary(
                com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_30D,
                Instant.parse("2026-02-07T00:59:30Z"),
                Instant.parse("2026-03-09T00:59:30Z"),
                30,
                new BigDecimal("0.95000000"),
                new BigDecimal("0.40350877"),
                4,
                10,
                16,
                8
        );
        AnalysisExternalRegimeTransition externalTransition = new AnalysisExternalRegimeTransition(
                com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference.D7,
                Instant.parse("2026-03-02T00:59:30Z"),
                AnalysisExternalRegimeTransitionType.TRANSITION_TO_HEADWIND,
                AnalysisExternalRegimeDirection.HEADWIND,
                AnalysisExternalRegimeSeverity.HIGH,
                new BigDecimal("1.08333333"),
                "D7 external regime transitioned to headwind."
        );
        AnalysisExternalRegimePersistence externalPersistence = new AnalysisExternalRegimePersistence(
                com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_30D,
                AnalysisExternalRegimeDirection.HEADWIND,
                new BigDecimal("0.53333333"),
                new BigDecimal("0.26666667"),
                new BigDecimal("0.45333333"),
                "LAST_30D keeps headwind dominance for 53.33% of samples with high severity on 26.67% of observations."
        );
        AnalysisExternalRegimeStatePayload externalState = new AnalysisExternalRegimeStatePayload(
                AnalysisExternalRegimeDirection.HEADWIND,
                AnalysisExternalRegimeSeverity.HIGH,
                AnalysisExternalRegimeCategory.MACRO,
                "Dollar strength regime",
                new BigDecimal("1.33333333"),
                new BigDecimal("0.59807018"),
                "External regime is headwind with high severity, primary signal Dollar strength regime, and reversal risk 0.6."
        );
        AnalysisExternalContextCompositePayload externalContextComposite = new AnalysisExternalContextCompositePayload(
                payload.marketContext().externalContextComposite().snapshotTime(),
                payload.marketContext().externalContextComposite().sourceDataVersion(),
                payload.marketContext().externalContextComposite().compositeRiskScore(),
                payload.marketContext().externalContextComposite().dominantDirection(),
                payload.marketContext().externalContextComposite().highestSeverity(),
                payload.marketContext().externalContextComposite().supportiveSignalCount(),
                payload.marketContext().externalContextComposite().cautionarySignalCount(),
                payload.marketContext().externalContextComposite().headwindSignalCount(),
                payload.marketContext().externalContextComposite().primarySignalCategory(),
                payload.marketContext().externalContextComposite().primarySignalTitle(),
                payload.marketContext().externalContextComposite().primarySignalDetail(),
                payload.marketContext().externalContextComposite().regimeSignals(),
                payload.marketContext().externalContextComposite().comparisonFacts(),
                payload.marketContext().externalContextComposite().highlights(),
                java.util.List.of(externalWindowSummary),
                java.util.List.of(externalTransition),
                externalPersistence,
                externalState
        );
        java.util.List<AnalysisPriceLevel> supportLevels = supportLevels();
        java.util.List<AnalysisPriceLevel> resistanceLevels = resistanceLevels();
        java.util.List<AnalysisPriceZone> supportZones = supportZones();
        java.util.List<AnalysisPriceZone> resistanceZones = resistanceZones();
        java.util.List<String> marketParticipationFacts = marketParticipationFacts();

        when(marketIndicatorSnapshotPersistenceService.createAndSaveContext("BTCUSDT", com.aicoinassist.batch.domain.market.enumtype.CandleInterval.FOUR_HOUR))
                .thenReturn(snapshotContext);
        when(analysisReportContinuityService.buildNotes("BTCUSDT", AnalysisReportType.MID_TERM, snapshot.getSnapshotTime()))
                .thenReturn(midTermContinuityNotes());
        when(marketContextSnapshotPersistenceService.createAndSave("BTCUSDT")).thenReturn(contextSnapshot);
        when(analysisDerivativeComparisonService.buildFacts(contextSnapshot, AnalysisReportType.MID_TERM))
                .thenReturn(derivativeContext.comparisonFacts());
        when(macroContextSnapshotPersistenceService.createAndSave()).thenReturn(macroContextSnapshot);
        when(macroContextWindowSummarySnapshotPersistenceService.createAndSaveForReportType(macroContextSnapshot, AnalysisReportType.MID_TERM))
                .thenReturn(generationMacroWindowSummaryEntities());
        when(analysisMacroComparisonService.buildFacts(macroContextSnapshot, AnalysisReportType.MID_TERM))
                .thenReturn(macroContext.comparisonFacts());
        when(sentimentSnapshotPersistenceService.createAndSaveFearGreedSnapshot()).thenReturn(sentimentSnapshot);
        when(sentimentWindowSummarySnapshotPersistenceService.createAndSaveForReportType(sentimentSnapshot, AnalysisReportType.MID_TERM))
                .thenReturn(generationSentimentWindowSummaryEntities());
        when(analysisSentimentComparisonService.buildFacts(sentimentSnapshot, AnalysisReportType.MID_TERM))
                .thenReturn(sentimentContext.comparisonFacts());
        when(onchainFactSnapshotPersistenceService.createAndSave("BTCUSDT")).thenReturn(onchainSnapshot);
        when(onchainWindowSummarySnapshotPersistenceService.createAndSaveForReportType(onchainSnapshot, AnalysisReportType.MID_TERM))
                .thenReturn(generationOnchainWindowSummaryEntities());
        when(analysisOnchainComparisonService.buildFacts(onchainSnapshot, AnalysisReportType.MID_TERM))
                .thenReturn(onchainContext.comparisonFacts());
        when(marketContextWindowSummarySnapshotPersistenceService.createAndSaveForReportType(contextSnapshot, AnalysisReportType.MID_TERM))
                .thenReturn(derivativeWindowSummaryEntities());
        when(marketWindowSummarySnapshotPersistenceService.createAndSaveForReportType(snapshot, AnalysisReportType.MID_TERM, snapshotContext.candles()))
                .thenReturn(windowSummaryEntities(snapshot));
        when(analysisMarketParticipationFactService.buildFacts(snapshot, AnalysisReportType.MID_TERM))
                .thenReturn(marketParticipationFacts);
        when(marketCandidateLevelSnapshotPersistenceService.createAndSaveAll(snapshot, snapshotContext.candles())).thenReturn(candidateLevelEntities());
        when(marketCandidateLevelZoneSnapshotPersistenceService.createAndSaveAll(anyList(), eq(snapshotContext.candles())))
                .thenReturn(candidateLevelZoneEntities());
        when(marketLevelContextSnapshotPersistenceService.createAndSave(eq(snapshot), anyList()))
                .thenReturn(levelContextSnapshot);
        when(analysisComparisonService.buildFacts(snapshot, AnalysisReportType.MID_TERM)).thenReturn(generationComparisonFacts());
        when(analysisLevelContextComparisonService.buildFacts(levelContextSnapshot, AnalysisReportType.MID_TERM))
                .thenReturn(payload.marketContext().levelContext().comparisonFacts());
        when(analysisReportMarketDataMapper.toDerivativeWindowSummary(any())).thenReturn(derivativeContext.windowSummaries().get(0));
        when(analysisReportMarketDataMapper.toDerivativeContext(contextSnapshot, derivativeContext.comparisonFacts(), derivativeContext.windowSummaries()))
                .thenReturn(derivativeContext);
        when(analysisReportMarketDataMapper.toMacroWindowSummary(any())).thenReturn(macroContext.windowSummaries().get(0));
        when(analysisReportMarketDataMapper.toMacroContext(macroContextSnapshot, macroContext.comparisonFacts(), macroContext.windowSummaries()))
                .thenReturn(macroContext);
        when(analysisReportMarketDataMapper.toSentimentWindowSummary(any())).thenReturn(sentimentContext.windowSummaries().get(0));
        when(analysisReportMarketDataMapper.toSentimentContext(sentimentSnapshot, sentimentContext.comparisonFacts(), sentimentContext.windowSummaries()))
                .thenReturn(sentimentContext);
        when(analysisReportMarketDataMapper.toOnchainWindowSummary(any())).thenReturn(onchainContext.windowSummaries().get(0));
        when(analysisReportMarketDataMapper.toOnchainContext(onchainSnapshot, onchainContext.comparisonFacts(), onchainContext.windowSummaries()))
                .thenReturn(onchainContext);
        when(analysisReportMarketDataMapper.toWindowSummary(any())).thenReturn(payload.windowSummaries().get(0));
        when(analysisReportMarketDataMapper.toCandidateLevels(anyList(), eq("SUPPORT"), any())).thenReturn(supportLevels);
        when(analysisReportMarketDataMapper.toCandidateLevels(anyList(), eq("RESISTANCE"), any())).thenReturn(resistanceLevels);
        when(analysisReportMarketDataMapper.toCandidateZones(anyList(), eq("SUPPORT"))).thenReturn(supportZones);
        when(analysisReportMarketDataMapper.toCandidateZones(anyList(), eq("RESISTANCE"))).thenReturn(resistanceZones);
        when(analysisReportMarketDataMapper.toLevelContext(
                levelContextSnapshot,
                supportZones,
                resistanceZones,
                payload.marketContext().levelContext().comparisonFacts()
        )).thenReturn(payload.marketContext().levelContext());
        when(analysisExternalContextSnapshotService.create(
                "BTCUSDT",
                AnalysisReportType.MID_TERM,
                derivativeContext,
                macroContext,
                sentimentContext,
                onchainContext
        )).thenReturn(externalContextSnapshotInput());
        when(marketExternalContextSnapshotPersistenceService.createAndSave(externalContextSnapshotInput()))
                .thenReturn(externalContextSnapshot);
        when(analysisExternalContextComparisonService.buildFacts(externalContextSnapshot, AnalysisReportType.MID_TERM))
                .thenReturn(payload.marketContext().externalContextComposite().comparisonFacts());
        when(analysisExternalContextComparisonService.buildHighlights(
                externalContextSnapshot,
                payload.marketContext().externalContextComposite().comparisonFacts()
        )).thenReturn(payload.marketContext().externalContextComposite().highlights());
        when(analysisExternalContextComparisonService.buildTransitions(
                externalContextSnapshot,
                payload.marketContext().externalContextComposite().comparisonFacts()
        )).thenReturn(java.util.List.of(externalTransition));
        when(marketExternalContextWindowSummarySnapshotPersistenceService.createAndSaveForReportType(
                externalContextSnapshot,
                AnalysisReportType.MID_TERM
        )).thenReturn(externalContextWindowSummaryEntities());
        when(analysisExternalContextComparisonService.buildPersistence(
                externalContextSnapshot,
                java.util.List.of(externalWindowSummary)
        )).thenReturn(externalPersistence);
        when(analysisExternalContextComparisonService.buildState(
                externalContextSnapshot,
                java.util.List.of(externalTransition),
                externalPersistence,
                java.util.List.of(externalWindowSummary)
        )).thenReturn(externalState);
        when(analysisReportMarketDataMapper.toExternalContextWindowSummary(any()))
                .thenReturn(externalWindowSummary);
        when(analysisReportMarketDataMapper.toExternalContextComposite(
                externalContextSnapshot,
                payload.marketContext().externalContextComposite().comparisonFacts(),
                payload.marketContext().externalContextComposite().highlights(),
                java.util.List.of(externalWindowSummary),
                java.util.List.of(externalTransition),
                externalPersistence,
                externalState
        ))
                .thenReturn(externalContextComposite);
        when(analysisReportAssembler.assemble(
                eq(snapshot),
                eq(AnalysisReportType.MID_TERM),
                eq(generationComparisonFacts()),
                eq(payload.windowSummaries()),
                eq(derivativeContext),
                eq(macroContext),
                eq(sentimentContext),
                eq(onchainContext),
                eq(midTermContinuityNotes()),
                eq(externalContextComposite),
                any(),
                eq(marketParticipationFacts),
                anyList(),
                anyList(),
                anyList(),
                anyList()
        )).thenReturn(payload);
        when(analysisReportPersistenceService.save(any(AnalysisReportDraft.class))).thenReturn(savedEntity);

        AnalysisReportEntity result = service.generateAndSave(
                "BTCUSDT",
                AnalysisReportType.MID_TERM,
                "gpt-5.4",
                Instant.parse("2026-03-09T01:00:30Z")
        );

        ArgumentCaptor<AnalysisReportDraft> draftCaptor = ArgumentCaptor.forClass(AnalysisReportDraft.class);
        verify(analysisReportPersistenceService).save(draftCaptor.capture());

        AnalysisReportDraft draft = draftCaptor.getValue();
        assertThat(draft.symbol()).isEqualTo("BTCUSDT");
        assertThat(draft.reportType()).isEqualTo(AnalysisReportType.MID_TERM);
        assertThat(draft.analysisBasisTime()).isEqualTo(snapshot.getSnapshotTime());
        assertThat(draft.rawReferenceTime()).isEqualTo(snapshot.getPriceSourceEventTime());
        assertThat(draft.sourceDataVersion()).isEqualTo(snapshot.getSourceDataVersion());
        assertThat(draft.analysisEngineVersion()).isEqualTo("gpt-5.4");
        assertThat(draft.reportPayload()).isSameAs(payload);
        assertThat(draft.storedTime()).isEqualTo(Instant.parse("2026-03-09T01:00:30Z"));
        assertThat(result).isSameAs(savedEntity);
    }
}
