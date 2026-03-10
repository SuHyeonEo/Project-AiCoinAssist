package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketLevelContextSnapshotEntity;
import com.aicoinassist.batch.domain.macro.entity.MacroContextSnapshotEntity;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceLevel;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceZone;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportDraft;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

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
        AnalysisReportPayload payload = midTermPayload();
        MarketContextSnapshotEntity contextSnapshot = marketContextSnapshotEntity();
        MarketLevelContextSnapshotEntity levelContextSnapshot = levelContextSnapshotEntity();
        MacroContextSnapshotEntity macroContextSnapshot = generationMacroContextSnapshot();
        SentimentSnapshotEntity sentimentSnapshot = generationSentimentSnapshot();
        AnalysisReportEntity savedEntity = savedReportEntity(snapshot);
        AnalysisDerivativeContext derivativeContext = generationDerivativeContextInput();
        AnalysisMacroContext macroContext = payload.macroContext();
        AnalysisSentimentContext sentimentContext = payload.sentimentContext();
        java.util.List<AnalysisPriceLevel> supportLevels = supportLevels();
        java.util.List<AnalysisPriceLevel> resistanceLevels = resistanceLevels();
        java.util.List<AnalysisPriceZone> supportZones = supportZones();
        java.util.List<AnalysisPriceZone> resistanceZones = resistanceZones();

        when(marketIndicatorSnapshotRepository.findTopBySymbolAndIntervalValueOrderBySnapshotTimeDescIdDesc("BTCUSDT", "4h"))
                .thenReturn(Optional.of(snapshot));
        when(analysisReportContinuityService.buildNotes("BTCUSDT", AnalysisReportType.MID_TERM, snapshot.getSnapshotTime()))
                .thenReturn(midTermContinuityNotes());
        when(marketContextSnapshotPersistenceService.createAndSave("BTCUSDT")).thenReturn(contextSnapshot);
        when(analysisDerivativeComparisonService.buildFacts(contextSnapshot, AnalysisReportType.MID_TERM))
                .thenReturn(derivativeContext.comparisonFacts());
        when(macroContextSnapshotPersistenceService.createAndSave()).thenReturn(macroContextSnapshot);
        when(analysisMacroComparisonService.buildFacts(macroContextSnapshot, AnalysisReportType.MID_TERM))
                .thenReturn(macroContext.comparisonFacts());
        when(sentimentSnapshotPersistenceService.createAndSaveFearGreedSnapshot()).thenReturn(sentimentSnapshot);
        when(analysisSentimentComparisonService.buildFacts(sentimentSnapshot, AnalysisReportType.MID_TERM))
                .thenReturn(sentimentContext.comparisonFacts());
        when(marketContextWindowSummarySnapshotPersistenceService.createAndSaveForReportType(contextSnapshot, AnalysisReportType.MID_TERM))
                .thenReturn(derivativeWindowSummaryEntities());
        when(marketWindowSummarySnapshotPersistenceService.createAndSaveForReportType(snapshot, AnalysisReportType.MID_TERM))
                .thenReturn(windowSummaryEntities(snapshot));
        when(marketCandidateLevelSnapshotPersistenceService.createAndSaveAll(snapshot)).thenReturn(candidateLevelEntities());
        when(marketCandidateLevelZoneSnapshotPersistenceService.createAndSaveAll(anyList()))
                .thenReturn(candidateLevelZoneEntities());
        when(marketLevelContextSnapshotPersistenceService.createAndSave(eq(snapshot), anyList()))
                .thenReturn(levelContextSnapshot);
        when(analysisComparisonService.buildFacts(snapshot, AnalysisReportType.MID_TERM)).thenReturn(generationComparisonFacts());
        when(analysisLevelContextComparisonService.buildFacts(levelContextSnapshot, AnalysisReportType.MID_TERM))
                .thenReturn(payload.marketContext().levelContext().comparisonFacts());
        when(analysisReportMarketDataMapper.toDerivativeWindowSummary(any())).thenReturn(derivativeContext.windowSummaries().get(0));
        when(analysisReportMarketDataMapper.toDerivativeContext(contextSnapshot, derivativeContext.comparisonFacts(), derivativeContext.windowSummaries()))
                .thenReturn(derivativeContext);
        when(analysisReportMarketDataMapper.toMacroContext(macroContextSnapshot, macroContext.comparisonFacts()))
                .thenReturn(macroContext);
        when(analysisReportMarketDataMapper.toSentimentContext(sentimentSnapshot, sentimentContext.comparisonFacts()))
                .thenReturn(sentimentContext);
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
        when(analysisReportAssembler.assemble(
                eq(snapshot),
                eq(AnalysisReportType.MID_TERM),
                eq(generationComparisonFacts()),
                eq(payload.windowSummaries()),
                eq(derivativeContext),
                eq(macroContext),
                eq(sentimentContext),
                eq(midTermContinuityNotes()),
                any(),
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
