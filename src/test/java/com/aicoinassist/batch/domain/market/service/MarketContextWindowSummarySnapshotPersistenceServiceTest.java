package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketContextWindowSummarySnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketContextWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.market.repository.MarketContextWindowSummarySnapshotRepository;
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
class MarketContextWindowSummarySnapshotPersistenceServiceTest {

    @Mock
    private MarketContextWindowSummarySnapshotService marketContextWindowSummarySnapshotService;

    @Mock
    private MarketContextWindowSummarySnapshotRepository marketContextWindowSummarySnapshotRepository;

    @Test
    void createAndSaveRefreshesExistingSummaryWhenKeyMatches() {
        MarketContextWindowSummarySnapshotPersistenceService service = new MarketContextWindowSummarySnapshotPersistenceService(
                marketContextWindowSummarySnapshotService,
                marketContextWindowSummarySnapshotRepository
        );

        MarketContextSnapshotEntity currentSnapshot = currentSnapshot();
        MarketContextWindowSummarySnapshot summary = summary(MarketWindowType.LAST_7D);
        MarketContextWindowSummarySnapshotEntity existingEntity = MarketContextWindowSummarySnapshotEntity.builder()
                                                                                                          .symbol("BTCUSDT")
                                                                                                          .windowType(MarketWindowType.LAST_7D.name())
                                                                                                          .windowStartTime(summary.windowStartTime())
                                                                                                          .windowEndTime(summary.windowEndTime())
                                                                                                          .sampleCount(10)
                                                                                                          .currentOpenInterest(new BigDecimal("12000.00000000"))
                                                                                                          .averageOpenInterest(new BigDecimal("11800.00000000"))
                                                                                                          .currentOpenInterestVsAverage(new BigDecimal("0.01694915"))
                                                                                                          .currentFundingRate(new BigDecimal("0.00030000"))
                                                                                                          .averageFundingRate(new BigDecimal("0.00022000"))
                                                                                                          .currentFundingVsAverage(new BigDecimal("0.36363636"))
                                                                                                          .currentBasisRate(new BigDecimal("0.09000000"))
                                                                                                          .averageBasisRate(new BigDecimal("0.06000000"))
                                                                                                          .currentBasisVsAverage(new BigDecimal("0.50000000"))
                                                                                                          .sourceDataVersion("old-version")
                                                                                                          .build();

        when(marketContextWindowSummarySnapshotService.create(currentSnapshot, MarketWindowType.LAST_7D))
                .thenReturn(summary);
        when(marketContextWindowSummarySnapshotRepository.findTopBySymbolAndWindowTypeAndWindowEndTimeOrderByIdDesc(
                "BTCUSDT",
                MarketWindowType.LAST_7D.name(),
                summary.windowEndTime()
        )).thenReturn(Optional.of(existingEntity));

        MarketContextWindowSummarySnapshotEntity result = service.createAndSave(currentSnapshot, MarketWindowType.LAST_7D);

        verify(marketContextWindowSummarySnapshotRepository, never()).save(any(MarketContextWindowSummarySnapshotEntity.class));
        assertThat(result).isSameAs(existingEntity);
        assertThat(existingEntity.getAverageOpenInterest()).isEqualByComparingTo("11000.00000000");
        assertThat(existingEntity.getCurrentFundingVsAverage()).isEqualByComparingTo("0.80000000");
    }

    @Test
    void createAndSaveForReportTypeBuildsExpectedWindowSet() {
        MarketContextWindowSummarySnapshotPersistenceService service = new MarketContextWindowSummarySnapshotPersistenceService(
                marketContextWindowSummarySnapshotService,
                marketContextWindowSummarySnapshotRepository
        );

        MarketContextSnapshotEntity currentSnapshot = currentSnapshot();
        when(marketContextWindowSummarySnapshotService.create(any(MarketContextSnapshotEntity.class), any(MarketWindowType.class)))
                .thenAnswer(invocation -> summary(invocation.getArgument(1)));
        when(marketContextWindowSummarySnapshotRepository.findTopBySymbolAndWindowTypeAndWindowEndTimeOrderByIdDesc(
                any(String.class),
                any(String.class),
                any(Instant.class)
        )).thenReturn(Optional.empty());
        when(marketContextWindowSummarySnapshotRepository.save(any(MarketContextWindowSummarySnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<MarketContextWindowSummarySnapshotEntity> entities = service.createAndSaveForReportType(
                currentSnapshot,
                AnalysisReportType.MID_TERM
        );

        assertThat(entities).extracting(MarketContextWindowSummarySnapshotEntity::getWindowType)
                            .containsExactly(
                                    MarketWindowType.LAST_7D.name(),
                                    MarketWindowType.LAST_14D.name(),
                                    MarketWindowType.LAST_30D.name()
                            );
    }

    private MarketContextSnapshotEntity currentSnapshot() {
        return MarketContextSnapshotEntity.builder()
                                          .symbol("BTCUSDT")
                                          .snapshotTime(Instant.parse("2026-03-09T00:59:30Z"))
                                          .openInterestSourceEventTime(Instant.parse("2026-03-09T00:59:00Z"))
                                          .premiumIndexSourceEventTime(Instant.parse("2026-03-09T00:59:30Z"))
                                          .sourceDataVersion("context-basis-key")
                                          .openInterest(new BigDecimal("12345.67890000"))
                                          .markPrice(new BigDecimal("87500.12000000"))
                                          .indexPrice(new BigDecimal("87480.02000000"))
                                          .lastFundingRate(new BigDecimal("0.00045000"))
                                          .nextFundingTime(Instant.parse("2026-03-09T08:00:00Z"))
                                          .markIndexBasisRate(new BigDecimal("0.12000000"))
                                          .build();
    }

    private MarketContextWindowSummarySnapshot summary(MarketWindowType windowType) {
        return new MarketContextWindowSummarySnapshot(
                "BTCUSDT",
                windowType,
                Instant.parse("2026-03-02T00:59:30Z"),
                Instant.parse("2026-03-09T00:59:30Z"),
                42,
                new BigDecimal("12345.67890000"),
                new BigDecimal("11000.00000000"),
                new BigDecimal("0.12233445"),
                new BigDecimal("0.00045000"),
                new BigDecimal("0.00025000"),
                new BigDecimal("0.80000000"),
                new BigDecimal("0.12000000"),
                new BigDecimal("0.07000000"),
                new BigDecimal("0.71428571"),
                "context-basis-key;windowType=" + windowType.name()
        );
    }
}
