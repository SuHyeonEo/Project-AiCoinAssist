package com.aicoinassist.batch.domain.onchain.service;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.onchain.dto.OnchainWindowSummarySnapshot;
import com.aicoinassist.batch.domain.onchain.entity.OnchainFactSnapshotEntity;
import com.aicoinassist.batch.domain.onchain.entity.OnchainWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.onchain.repository.OnchainWindowSummarySnapshotRepository;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnchainWindowSummarySnapshotPersistenceService {

    private final OnchainWindowSummarySnapshotService onchainWindowSummarySnapshotService;
    private final OnchainWindowSummarySnapshotRepository onchainWindowSummarySnapshotRepository;

    @Transactional
    public List<OnchainWindowSummarySnapshotEntity> createAndSaveForReportType(
            OnchainFactSnapshotEntity currentSnapshot,
            AnalysisReportType reportType
    ) {
        List<OnchainWindowSummarySnapshotEntity> entities = new ArrayList<>();
        for (MarketWindowType windowType : windowTypes(reportType)) {
            entities.add(createAndSave(currentSnapshot, windowType));
        }
        return entities;
    }

    @Transactional
    public OnchainWindowSummarySnapshotEntity createAndSave(
            OnchainFactSnapshotEntity currentSnapshot,
            MarketWindowType windowType
    ) {
        OnchainWindowSummarySnapshot summary = onchainWindowSummarySnapshotService.create(currentSnapshot, windowType);

        OnchainWindowSummarySnapshotEntity existingEntity = onchainWindowSummarySnapshotRepository
                .findTopBySymbolAndWindowTypeAndWindowEndTimeOrderByIdDesc(
                        summary.symbol(),
                        summary.windowType().name(),
                        summary.windowEndTime()
                )
                .orElse(null);

        if (existingEntity == null) {
            OnchainWindowSummarySnapshotEntity entity = OnchainWindowSummarySnapshotEntity.builder()
                                                                                          .symbol(summary.symbol())
                                                                                          .assetCode(summary.assetCode())
                                                                                          .windowType(summary.windowType().name())
                                                                                          .windowStartTime(summary.windowStartTime())
                                                                                          .windowEndTime(summary.windowEndTime())
                                                                                          .sampleCount(summary.sampleCount())
                                                                                          .currentActiveAddressCount(summary.currentActiveAddressCount())
                                                                                          .averageActiveAddressCount(summary.averageActiveAddressCount())
                                                                                          .currentActiveAddressVsAverage(summary.currentActiveAddressVsAverage())
                                                                                          .currentTransactionCount(summary.currentTransactionCount())
                                                                                          .averageTransactionCount(summary.averageTransactionCount())
                                                                                          .currentTransactionCountVsAverage(summary.currentTransactionCountVsAverage())
                                                                                          .currentMarketCapUsd(summary.currentMarketCapUsd())
                                                                                          .averageMarketCapUsd(summary.averageMarketCapUsd())
                                                                                          .currentMarketCapVsAverage(summary.currentMarketCapVsAverage())
                                                                                          .sourceDataVersion(summary.sourceDataVersion())
                                                                                          .build();
            return onchainWindowSummarySnapshotRepository.save(entity);
        }

        existingEntity.refreshFromSummary(
                summary.windowStartTime(),
                summary.sampleCount(),
                summary.currentActiveAddressCount(),
                summary.averageActiveAddressCount(),
                summary.currentActiveAddressVsAverage(),
                summary.currentTransactionCount(),
                summary.averageTransactionCount(),
                summary.currentTransactionCountVsAverage(),
                summary.currentMarketCapUsd(),
                summary.averageMarketCapUsd(),
                summary.currentMarketCapVsAverage(),
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
