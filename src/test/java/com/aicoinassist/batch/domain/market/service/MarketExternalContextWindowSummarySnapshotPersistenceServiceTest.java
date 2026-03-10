package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketExternalContextWindowSummarySnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketExternalContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketExternalContextWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.market.repository.MarketExternalContextWindowSummarySnapshotRepository;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketExternalContextWindowSummarySnapshotPersistenceServiceTest {

    @Mock
    private MarketExternalContextWindowSummarySnapshotService marketExternalContextWindowSummarySnapshotService;

    @Mock
    private MarketExternalContextWindowSummarySnapshotRepository marketExternalContextWindowSummarySnapshotRepository;

    @InjectMocks
    private MarketExternalContextWindowSummarySnapshotPersistenceService service;

    @Test
    void createAndSavePersistsNewSummaryWhenIdentityDoesNotExist() {
        MarketExternalContextSnapshotEntity currentSnapshot = currentSnapshot();
        MarketExternalContextWindowSummarySnapshot summary = summary(MarketWindowType.LAST_30D, "0.40350877");

        when(marketExternalContextWindowSummarySnapshotService.create(currentSnapshot, MarketWindowType.LAST_30D))
                .thenReturn(summary);
        when(marketExternalContextWindowSummarySnapshotRepository
                .findTopBySymbolAndWindowTypeAndWindowEndTimeOrderByIdDesc(
                        "BTCUSDT",
                        MarketWindowType.LAST_30D.name(),
                        Instant.parse("2026-03-09T00:59:30Z")
                ))
                .thenReturn(Optional.empty());
        when(marketExternalContextWindowSummarySnapshotRepository.save(any(MarketExternalContextWindowSummarySnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MarketExternalContextWindowSummarySnapshotEntity result = service.createAndSave(currentSnapshot, MarketWindowType.LAST_30D);

        ArgumentCaptor<MarketExternalContextWindowSummarySnapshotEntity> entityCaptor =
                ArgumentCaptor.forClass(MarketExternalContextWindowSummarySnapshotEntity.class);
        verify(marketExternalContextWindowSummarySnapshotRepository).save(entityCaptor.capture());
        assertThat(result.getWindowType()).isEqualTo(MarketWindowType.LAST_30D.name());
        assertThat(entityCaptor.getValue().getCurrentCompositeRiskVsAverage()).isEqualByComparingTo("0.40350877");
    }

    @Test
    void createAndSaveRefreshesExistingSummaryWhenIdentityMatches() {
        MarketExternalContextSnapshotEntity currentSnapshot = currentSnapshot();
        MarketExternalContextWindowSummarySnapshot summary = summary(MarketWindowType.LAST_7D, "0.19402985");
        MarketExternalContextWindowSummarySnapshotEntity existingEntity = MarketExternalContextWindowSummarySnapshotEntity.builder()
                .symbol("BTCUSDT")
                .windowType(MarketWindowType.LAST_7D.name())
                .windowStartTime(Instant.parse("2026-03-01T00:59:30Z"))
                .windowEndTime(Instant.parse("2026-03-09T00:59:30Z"))
                .sampleCount(8)
                .currentCompositeRiskScore(new BigDecimal("1.10000000"))
                .averageCompositeRiskScore(new BigDecimal("0.92000000"))
                .currentCompositeRiskVsAverage(new BigDecimal("0.19565217"))
                .supportiveDominanceSampleCount(2)
                .cautionaryDominanceSampleCount(3)
                .headwindDominanceSampleCount(3)
                .highSeveritySampleCount(1)
                .sourceDataVersion("old")
                .build();

        when(marketExternalContextWindowSummarySnapshotService.create(currentSnapshot, MarketWindowType.LAST_7D))
                .thenReturn(summary);
        when(marketExternalContextWindowSummarySnapshotRepository
                .findTopBySymbolAndWindowTypeAndWindowEndTimeOrderByIdDesc(
                        "BTCUSDT",
                        MarketWindowType.LAST_7D.name(),
                        Instant.parse("2026-03-09T00:59:30Z")
                ))
                .thenReturn(Optional.of(existingEntity));

        MarketExternalContextWindowSummarySnapshotEntity result = service.createAndSave(currentSnapshot, MarketWindowType.LAST_7D);

        verify(marketExternalContextWindowSummarySnapshotRepository, never()).save(any());
        assertThat(result).isSameAs(existingEntity);
        assertThat(existingEntity.getSampleCount()).isEqualTo(7);
        assertThat(existingEntity.getCurrentCompositeRiskVsAverage()).isEqualByComparingTo("0.19402985");
        assertThat(existingEntity.getSourceDataVersion()).contains("windowType=LAST_7D");
    }

    @Test
    void windowTypesMatchesReportHorizonPolicy() {
        assertThat(service.windowTypes(AnalysisReportType.SHORT_TERM))
                .containsExactly(MarketWindowType.LAST_1D, MarketWindowType.LAST_3D, MarketWindowType.LAST_7D);
        assertThat(service.windowTypes(AnalysisReportType.MID_TERM))
                .containsExactly(MarketWindowType.LAST_7D, MarketWindowType.LAST_14D, MarketWindowType.LAST_30D);
        assertThat(service.windowTypes(AnalysisReportType.LONG_TERM))
                .containsExactly(MarketWindowType.LAST_30D, MarketWindowType.LAST_90D, MarketWindowType.LAST_180D, MarketWindowType.LAST_52W);
    }

    private MarketExternalContextSnapshotEntity currentSnapshot() {
        Instant snapshotTime = Instant.parse("2026-03-09T00:59:30Z");
        return MarketExternalContextSnapshotEntity.builder()
                .symbol("BTCUSDT")
                .snapshotTime(snapshotTime)
                .derivativeSnapshotTime(snapshotTime)
                .macroSnapshotTime(snapshotTime)
                .sentimentSnapshotTime(snapshotTime)
                .onchainSnapshotTime(snapshotTime)
                .sourceDataVersion("external-context-v3")
                .compositeRiskScore(new BigDecimal("1.33333333"))
                .dominantDirection("HEADWIND")
                .highestSeverity("HIGH")
                .supportiveSignalCount(0)
                .cautionarySignalCount(2)
                .headwindSignalCount(2)
                .primarySignalCategory("MACRO")
                .primarySignalTitle("Dollar strength regime")
                .primarySignalDetail("DXY remains firm.")
                .regimeSignalsPayload("[]")
                .build();
    }

    private MarketExternalContextWindowSummarySnapshot summary(MarketWindowType windowType, String currentCompositeRiskVsAverage) {
        return new MarketExternalContextWindowSummarySnapshot(
                "BTCUSDT",
                windowType,
                Instant.parse("2026-02-07T00:59:30Z"),
                Instant.parse("2026-03-09T00:59:30Z"),
                7,
                new BigDecimal("1.33333333"),
                new BigDecimal("1.11666667"),
                new BigDecimal(currentCompositeRiskVsAverage),
                1,
                2,
                4,
                3,
                "external-window-basis;windowType=" + windowType.name()
        );
    }
}
