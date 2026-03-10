package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketExternalContextSnapshot;
import com.aicoinassist.batch.domain.market.dto.MarketExternalRegimeSignalSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketExternalContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.repository.MarketExternalContextSnapshotRepository;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeDirection;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeSeverity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarketExternalContextSnapshotPersistenceServiceTest {

    @Mock
    private MarketExternalContextSnapshotRepository marketExternalContextSnapshotRepository;

    @Test
    void createAndSaveRefreshesExistingSnapshot() {
        MarketExternalContextSnapshotPersistenceService marketExternalContextSnapshotPersistenceService =
                new MarketExternalContextSnapshotPersistenceService(marketExternalContextSnapshotRepository, new ObjectMapper());
        Instant snapshotTime = Instant.parse("2026-03-11T00:00:00Z");
        MarketExternalContextSnapshot snapshot = snapshot(snapshotTime, "0.66666667");
        MarketExternalContextSnapshotEntity existingEntity = MarketExternalContextSnapshotEntity.builder()
                                                                                                .symbol("BTCUSDT")
                                                                                                .snapshotTime(snapshotTime)
                                                                                                .derivativeSnapshotTime(snapshotTime.minusSeconds(60))
                                                                                                .macroSnapshotTime(snapshotTime.minusSeconds(120))
                                                                                                .sentimentSnapshotTime(snapshotTime.minusSeconds(180))
                                                                                                .onchainSnapshotTime(snapshotTime.minusSeconds(240))
                                                                                                .sourceDataVersion("derivative=v0;macro=v0;sentiment=v0;onchain=v0")
                                                                                                .compositeRiskScore(new BigDecimal("0.10000000"))
                                                                                                .dominantDirection("CAUTIONARY")
                                                                                                .highestSeverity("MEDIUM")
                                                                                                .supportiveSignalCount(0)
                                                                                                .cautionarySignalCount(1)
                                                                                                .headwindSignalCount(0)
                                                                                                .primarySignalCategory("MACRO")
                                                                                                .primarySignalTitle("Old regime")
                                                                                                .primarySignalDetail("Old detail")
                                                                                                .regimeSignalsPayload("[]")
                                                                                                .build();

        when(marketExternalContextSnapshotRepository.findTopBySymbolAndSnapshotTimeOrderByIdDesc("BTCUSDT", snapshotTime))
                .thenReturn(Optional.of(existingEntity));

        MarketExternalContextSnapshotEntity result = marketExternalContextSnapshotPersistenceService.createAndSave(snapshot);

        verify(marketExternalContextSnapshotRepository, never()).save(any(MarketExternalContextSnapshotEntity.class));
        assertThat(result.getCompositeRiskScore()).isEqualByComparingTo("0.66666667");
        assertThat(result.getPrimarySignalTitle()).isEqualTo("Dollar strength regime");
        assertThat(result.getRegimeSignalsPayload()).contains("Dollar strength regime");
    }

    @Test
    void createAndSavePersistsNewSnapshot() {
        MarketExternalContextSnapshotPersistenceService marketExternalContextSnapshotPersistenceService =
                new MarketExternalContextSnapshotPersistenceService(marketExternalContextSnapshotRepository, new ObjectMapper());
        Instant snapshotTime = Instant.parse("2026-03-11T00:00:00Z");
        MarketExternalContextSnapshot snapshot = snapshot(snapshotTime, "0.66666667");

        when(marketExternalContextSnapshotRepository.findTopBySymbolAndSnapshotTimeOrderByIdDesc("BTCUSDT", snapshotTime))
                .thenReturn(Optional.empty());
        when(marketExternalContextSnapshotRepository.save(any(MarketExternalContextSnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MarketExternalContextSnapshotEntity result = marketExternalContextSnapshotPersistenceService.createAndSave(snapshot);

        assertThat(result.getPrimarySignalTitle()).isEqualTo("Dollar strength regime");
        assertThat(result.getDominantDirection()).isEqualTo("HEADWIND");
        assertThat(result.getRegimeSignalsPayload()).contains("Yield pressure regime");
    }

    private MarketExternalContextSnapshot snapshot(Instant snapshotTime, String compositeRiskScore) {
        return new MarketExternalContextSnapshot(
                "BTCUSDT",
                snapshotTime,
                snapshotTime.minusSeconds(60),
                snapshotTime.minusSeconds(120),
                snapshotTime.minusSeconds(180),
                snapshotTime.minusSeconds(240),
                "derivative=v1;macro=v1;sentiment=v1;onchain=v1",
                new BigDecimal(compositeRiskScore),
                AnalysisExternalRegimeDirection.HEADWIND,
                AnalysisExternalRegimeSeverity.HIGH,
                0,
                1,
                2,
                AnalysisExternalRegimeCategory.MACRO,
                "Dollar strength regime",
                "Dollar and yields remain firm.",
                List.of(
                        new MarketExternalRegimeSignalSnapshot(
                                AnalysisExternalRegimeCategory.MACRO,
                                "Dollar strength regime",
                                "DXY remains above average.",
                                AnalysisExternalRegimeDirection.HEADWIND,
                                AnalysisExternalRegimeSeverity.HIGH,
                                "LAST_30D"
                        ),
                        new MarketExternalRegimeSignalSnapshot(
                                AnalysisExternalRegimeCategory.MACRO,
                                "Yield pressure regime",
                                "US10Y remains above average.",
                                AnalysisExternalRegimeDirection.CAUTIONARY,
                                AnalysisExternalRegimeSeverity.MEDIUM,
                                "LAST_30D"
                        )
                )
        );
    }
}
