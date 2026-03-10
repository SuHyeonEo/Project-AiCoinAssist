package com.aicoinassist.batch.domain.macro.service;

import com.aicoinassist.batch.domain.macro.dto.MacroContextWindowSummarySnapshot;
import com.aicoinassist.batch.domain.macro.entity.MacroContextSnapshotEntity;
import com.aicoinassist.batch.domain.macro.entity.MacroContextWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.macro.repository.MacroContextWindowSummarySnapshotRepository;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MacroContextWindowSummarySnapshotPersistenceService {

    private final MacroContextWindowSummarySnapshotService macroContextWindowSummarySnapshotService;
    private final MacroContextWindowSummarySnapshotRepository macroContextWindowSummarySnapshotRepository;

    @Transactional
    public List<MacroContextWindowSummarySnapshotEntity> createAndSaveForReportType(
            MacroContextSnapshotEntity currentSnapshot,
            AnalysisReportType reportType
    ) {
        List<MacroContextWindowSummarySnapshotEntity> entities = new ArrayList<>();
        for (MarketWindowType windowType : windowTypes(reportType)) {
            entities.add(createAndSave(currentSnapshot, windowType));
        }
        return entities;
    }

    @Transactional
    public MacroContextWindowSummarySnapshotEntity createAndSave(
            MacroContextSnapshotEntity currentSnapshot,
            MarketWindowType windowType
    ) {
        MacroContextWindowSummarySnapshot summary = macroContextWindowSummarySnapshotService.create(currentSnapshot, windowType);

        MacroContextWindowSummarySnapshotEntity existingEntity = macroContextWindowSummarySnapshotRepository
                .findTopByWindowTypeAndWindowEndTimeOrderByIdDesc(
                        summary.windowType().name(),
                        summary.windowEndTime()
                )
                .orElse(null);

        if (existingEntity == null) {
            MacroContextWindowSummarySnapshotEntity entity = MacroContextWindowSummarySnapshotEntity.builder()
                                                                                                    .windowType(summary.windowType().name())
                                                                                                    .windowStartTime(summary.windowStartTime())
                                                                                                    .windowEndTime(summary.windowEndTime())
                                                                                                    .sampleCount(summary.sampleCount())
                                                                                                    .currentDxyProxyValue(summary.currentDxyProxyValue())
                                                                                                    .averageDxyProxyValue(summary.averageDxyProxyValue())
                                                                                                    .currentDxyProxyVsAverage(summary.currentDxyProxyVsAverage())
                                                                                                    .currentUs10yYieldValue(summary.currentUs10yYieldValue())
                                                                                                    .averageUs10yYieldValue(summary.averageUs10yYieldValue())
                                                                                                    .currentUs10yYieldVsAverage(summary.currentUs10yYieldVsAverage())
                                                                                                    .currentUsdKrwValue(summary.currentUsdKrwValue())
                                                                                                    .averageUsdKrwValue(summary.averageUsdKrwValue())
                                                                                                    .currentUsdKrwVsAverage(summary.currentUsdKrwVsAverage())
                                                                                                    .sourceDataVersion(summary.sourceDataVersion())
                                                                                                    .build();
            return macroContextWindowSummarySnapshotRepository.save(entity);
        }

        existingEntity.refreshFromSummary(
                summary.windowStartTime(),
                summary.sampleCount(),
                summary.currentDxyProxyValue(),
                summary.averageDxyProxyValue(),
                summary.currentDxyProxyVsAverage(),
                summary.currentUs10yYieldValue(),
                summary.averageUs10yYieldValue(),
                summary.currentUs10yYieldVsAverage(),
                summary.currentUsdKrwValue(),
                summary.averageUsdKrwValue(),
                summary.currentUsdKrwVsAverage(),
                summary.sourceDataVersion()
        );
        return existingEntity;
    }

    public List<MarketWindowType> windowTypes(AnalysisReportType reportType) {
        return switch (reportType) {
            case SHORT_TERM -> List.of(MarketWindowType.LAST_1D, MarketWindowType.LAST_3D, MarketWindowType.LAST_7D);
            case MID_TERM -> List.of(MarketWindowType.LAST_7D, MarketWindowType.LAST_14D, MarketWindowType.LAST_30D);
            case LONG_TERM -> List.of(MarketWindowType.LAST_30D, MarketWindowType.LAST_90D, MarketWindowType.LAST_180D, MarketWindowType.LAST_52W);
        };
    }
}
