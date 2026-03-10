package com.aicoinassist.batch.domain.onchain.service;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.onchain.dto.OnchainWindowSummarySnapshot;
import com.aicoinassist.batch.domain.onchain.entity.OnchainFactSnapshotEntity;
import com.aicoinassist.batch.domain.onchain.entity.OnchainWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.onchain.repository.OnchainWindowSummarySnapshotRepository;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
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
class OnchainWindowSummarySnapshotPersistenceServiceTest {

    @Mock
    private OnchainWindowSummarySnapshotService onchainWindowSummarySnapshotService;

    @Mock
    private OnchainWindowSummarySnapshotRepository onchainWindowSummarySnapshotRepository;

    @Test
    void createAndSaveRefreshesExistingSummaryWhenKeyMatches() {
        OnchainWindowSummarySnapshotPersistenceService service = new OnchainWindowSummarySnapshotPersistenceService(
                onchainWindowSummarySnapshotService,
                onchainWindowSummarySnapshotRepository
        );

        OnchainFactSnapshotEntity currentSnapshot = currentSnapshot();
        OnchainWindowSummarySnapshot summary = summary(MarketWindowType.LAST_30D);
        OnchainWindowSummarySnapshotEntity existingEntity = OnchainWindowSummarySnapshotEntity.builder()
                                                                                              .symbol("BTCUSDT")
                                                                                              .assetCode("btc")
                                                                                              .windowType(MarketWindowType.LAST_30D.name())
                                                                                              .windowStartTime(summary.windowStartTime())
                                                                                              .windowEndTime(summary.windowEndTime())
                                                                                              .sampleCount(25)
                                                                                              .currentActiveAddressCount(new BigDecimal("1050000.00000000"))
                                                                                              .averageActiveAddressCount(new BigDecimal("970000.00000000"))
                                                                                              .currentActiveAddressVsAverage(new BigDecimal("0.08247423"))
                                                                                              .currentTransactionCount(new BigDecimal("525000.00000000"))
                                                                                              .averageTransactionCount(new BigDecimal("495000.00000000"))
                                                                                              .currentTransactionCountVsAverage(new BigDecimal("0.06060606"))
                                                                                              .currentMarketCapUsd(new BigDecimal("1700000000000.00000000"))
                                                                                              .averageMarketCapUsd(new BigDecimal("1640000000000.00000000"))
                                                                                              .currentMarketCapVsAverage(new BigDecimal("0.03658537"))
                                                                                              .sourceDataVersion("old-version")
                                                                                              .build();

        when(onchainWindowSummarySnapshotService.create(currentSnapshot, MarketWindowType.LAST_30D))
                .thenReturn(summary);
        when(onchainWindowSummarySnapshotRepository.findTopBySymbolAndWindowTypeAndWindowEndTimeOrderByIdDesc(
                "BTCUSDT",
                MarketWindowType.LAST_30D.name(),
                summary.windowEndTime()
        )).thenReturn(Optional.of(existingEntity));

        OnchainWindowSummarySnapshotEntity result = service.createAndSave(currentSnapshot, MarketWindowType.LAST_30D);

        verify(onchainWindowSummarySnapshotRepository, never()).save(any(OnchainWindowSummarySnapshotEntity.class));
        assertThat(result).isSameAs(existingEntity);
        assertThat(existingEntity.getAverageActiveAddressCount()).isEqualByComparingTo("980000.00000000");
        assertThat(existingEntity.getCurrentMarketCapVsAverage()).isEqualByComparingTo("0.03030303");
    }

    @Test
    void createAndSaveForReportTypeBuildsExpectedWindowSet() {
        OnchainWindowSummarySnapshotPersistenceService service = new OnchainWindowSummarySnapshotPersistenceService(
                onchainWindowSummarySnapshotService,
                onchainWindowSummarySnapshotRepository
        );

        OnchainFactSnapshotEntity currentSnapshot = currentSnapshot();
        when(onchainWindowSummarySnapshotService.create(any(OnchainFactSnapshotEntity.class), any(MarketWindowType.class)))
                .thenAnswer(invocation -> summary(invocation.getArgument(1)));
        when(onchainWindowSummarySnapshotRepository.findTopBySymbolAndWindowTypeAndWindowEndTimeOrderByIdDesc(
                any(String.class),
                any(String.class),
                any(Instant.class)
        )).thenReturn(Optional.empty());
        when(onchainWindowSummarySnapshotRepository.save(any(OnchainWindowSummarySnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<OnchainWindowSummarySnapshotEntity> entities = service.createAndSaveForReportType(
                currentSnapshot,
                AnalysisReportType.MID_TERM
        );

        assertThat(entities).extracting(OnchainWindowSummarySnapshotEntity::getWindowType)
                            .containsExactly(
                                    MarketWindowType.LAST_7D.name(),
                                    MarketWindowType.LAST_14D.name(),
                                    MarketWindowType.LAST_30D.name()
                            );
    }

    private OnchainFactSnapshotEntity currentSnapshot() {
        return OnchainFactSnapshotEntity.builder()
                                        .symbol("BTCUSDT")
                                        .assetCode("btc")
                                        .snapshotTime(Instant.parse("2026-03-09T00:00:00Z"))
                                        .activeAddressSourceEventTime(Instant.parse("2026-03-09T00:00:00Z"))
                                        .transactionCountSourceEventTime(Instant.parse("2026-03-09T00:00:00Z"))
                                        .marketCapSourceEventTime(Instant.parse("2026-03-09T00:00:00Z"))
                                        .sourceDataVersion("activeAddressDate=2026-03-09;transactionCountDate=2026-03-09;marketCapDate=2026-03-09")
                                        .activeAddressCount(new BigDecimal("1050000.00000000"))
                                        .transactionCount(new BigDecimal("525000.00000000"))
                                        .marketCapUsd(new BigDecimal("1700000000000.00000000"))
                                        .build();
    }

    private OnchainWindowSummarySnapshot summary(MarketWindowType windowType) {
        return new OnchainWindowSummarySnapshot(
                "BTCUSDT",
                "btc",
                windowType,
                Instant.parse("2026-02-07T00:00:00Z"),
                Instant.parse("2026-03-09T00:00:00Z"),
                30,
                new BigDecimal("1050000.00000000"),
                new BigDecimal("980000.00000000"),
                new BigDecimal("0.07142857"),
                new BigDecimal("525000.00000000"),
                new BigDecimal("500000.00000000"),
                new BigDecimal("0.05000000"),
                new BigDecimal("1700000000000.00000000"),
                new BigDecimal("1650000000000.00000000"),
                new BigDecimal("0.03030303"),
                "symbol=BTCUSDT;windowType=" + windowType.name()
        );
    }
}
