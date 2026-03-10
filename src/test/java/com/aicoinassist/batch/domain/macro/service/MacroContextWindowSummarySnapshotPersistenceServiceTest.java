package com.aicoinassist.batch.domain.macro.service;

import com.aicoinassist.batch.domain.macro.dto.MacroContextWindowSummarySnapshot;
import com.aicoinassist.batch.domain.macro.entity.MacroContextSnapshotEntity;
import com.aicoinassist.batch.domain.macro.entity.MacroContextWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.macro.repository.MacroContextWindowSummarySnapshotRepository;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MacroContextWindowSummarySnapshotPersistenceServiceTest {

    @Mock
    private MacroContextWindowSummarySnapshotService macroContextWindowSummarySnapshotService;

    @Mock
    private MacroContextWindowSummarySnapshotRepository macroContextWindowSummarySnapshotRepository;

    @Test
    void createAndSaveRefreshesExistingSummaryWhenKeyMatches() {
        MacroContextWindowSummarySnapshotPersistenceService service = new MacroContextWindowSummarySnapshotPersistenceService(
                macroContextWindowSummarySnapshotService,
                macroContextWindowSummarySnapshotRepository
        );

        MacroContextSnapshotEntity currentSnapshot = currentSnapshot();
        MacroContextWindowSummarySnapshot summary = summary(MarketWindowType.LAST_30D);
        MacroContextWindowSummarySnapshotEntity existingEntity = MacroContextWindowSummarySnapshotEntity.builder()
                                                                                                        .windowType(MarketWindowType.LAST_30D.name())
                                                                                                        .windowStartTime(summary.windowStartTime())
                                                                                                        .windowEndTime(summary.windowEndTime())
                                                                                                        .sampleCount(20)
                                                                                                        .currentDxyProxyValue(new BigDecimal("119.84210000"))
                                                                                                        .averageDxyProxyValue(new BigDecimal("118.50000000"))
                                                                                                        .currentDxyProxyVsAverage(new BigDecimal("0.01132574"))
                                                                                                        .currentUs10yYieldValue(new BigDecimal("4.12000000"))
                                                                                                        .averageUs10yYieldValue(new BigDecimal("3.95000000"))
                                                                                                        .currentUs10yYieldVsAverage(new BigDecimal("0.04303797"))
                                                                                                        .currentUsdKrwValue(new BigDecimal("1453.22000000"))
                                                                                                        .averageUsdKrwValue(new BigDecimal("1425.00000000"))
                                                                                                        .currentUsdKrwVsAverage(new BigDecimal("0.01980351"))
                                                                                                        .sourceDataVersion("old-version")
                                                                                                        .build();

        when(macroContextWindowSummarySnapshotService.create(currentSnapshot, MarketWindowType.LAST_30D))
                .thenReturn(summary);
        when(macroContextWindowSummarySnapshotRepository.findTopByWindowTypeAndWindowEndTimeOrderByIdDesc(
                MarketWindowType.LAST_30D.name(),
                summary.windowEndTime()
        )).thenReturn(Optional.of(existingEntity));

        MacroContextWindowSummarySnapshotEntity result = service.createAndSave(currentSnapshot, MarketWindowType.LAST_30D);

        verify(macroContextWindowSummarySnapshotRepository, never()).save(any(MacroContextWindowSummarySnapshotEntity.class));
        assertThat(result).isSameAs(existingEntity);
        assertThat(existingEntity.getAverageDxyProxyValue()).isEqualByComparingTo("119.00000000");
        assertThat(existingEntity.getCurrentUsdKrwVsAverage()).isEqualByComparingTo("0.01623776");
    }

    @Test
    void createAndSaveForReportTypeBuildsExpectedWindowSet() {
        MacroContextWindowSummarySnapshotPersistenceService service = new MacroContextWindowSummarySnapshotPersistenceService(
                macroContextWindowSummarySnapshotService,
                macroContextWindowSummarySnapshotRepository
        );

        MacroContextSnapshotEntity currentSnapshot = currentSnapshot();
        when(macroContextWindowSummarySnapshotService.create(any(MacroContextSnapshotEntity.class), any(MarketWindowType.class)))
                .thenAnswer(invocation -> summary(invocation.getArgument(1)));
        when(macroContextWindowSummarySnapshotRepository.findTopByWindowTypeAndWindowEndTimeOrderByIdDesc(
                any(String.class),
                any(Instant.class)
        )).thenReturn(Optional.empty());
        when(macroContextWindowSummarySnapshotRepository.save(any(MacroContextWindowSummarySnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<MacroContextWindowSummarySnapshotEntity> entities = service.createAndSaveForReportType(
                currentSnapshot,
                AnalysisReportType.MID_TERM
        );

        assertThat(entities).extracting(MacroContextWindowSummarySnapshotEntity::getWindowType)
                            .containsExactly(
                                    MarketWindowType.LAST_7D.name(),
                                    MarketWindowType.LAST_14D.name(),
                                    MarketWindowType.LAST_30D.name()
                            );
    }

    private MacroContextSnapshotEntity currentSnapshot() {
        return MacroContextSnapshotEntity.builder()
                                         .snapshotTime(Instant.parse("2026-03-09T00:00:00Z"))
                                         .dxyObservationDate(LocalDate.parse("2026-03-09"))
                                         .us10yYieldObservationDate(LocalDate.parse("2026-03-09"))
                                         .usdKrwObservationDate(LocalDate.parse("2026-03-09"))
                                         .sourceDataVersion("dxyProxyDate=2026-03-09;us10yYieldDate=2026-03-09;usdKrwDate=2026-03-09")
                                         .dxyProxyValue(new BigDecimal("119.84210000"))
                                         .us10yYieldValue(new BigDecimal("4.12000000"))
                                         .usdKrwValue(new BigDecimal("1453.22000000"))
                                         .build();
    }

    private MacroContextWindowSummarySnapshot summary(MarketWindowType windowType) {
        return new MacroContextWindowSummarySnapshot(
                windowType,
                Instant.parse("2026-02-07T00:00:00Z"),
                Instant.parse("2026-03-09T00:00:00Z"),
                30,
                new BigDecimal("119.84210000"),
                new BigDecimal("119.00000000"),
                new BigDecimal("0.00707647"),
                new BigDecimal("4.12000000"),
                new BigDecimal("4.00000000"),
                new BigDecimal("0.03000000"),
                new BigDecimal("1453.22000000"),
                new BigDecimal("1430.00000000"),
                new BigDecimal("0.01623776"),
                "windowType=" + windowType.name()
        );
    }
}
