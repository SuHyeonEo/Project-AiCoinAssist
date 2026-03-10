package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketExternalContextWindowSummarySnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketExternalContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketExternalContextWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.market.repository.MarketExternalContextWindowSummarySnapshotRepository;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MarketExternalContextWindowSummarySnapshotPersistenceService {

    private final MarketExternalContextWindowSummarySnapshotService marketExternalContextWindowSummarySnapshotService;
    private final MarketExternalContextWindowSummarySnapshotRepository marketExternalContextWindowSummarySnapshotRepository;

    public MarketExternalContextWindowSummarySnapshotPersistenceService(
            MarketExternalContextWindowSummarySnapshotService marketExternalContextWindowSummarySnapshotService,
            MarketExternalContextWindowSummarySnapshotRepository marketExternalContextWindowSummarySnapshotRepository
    ) {
        this.marketExternalContextWindowSummarySnapshotService = marketExternalContextWindowSummarySnapshotService;
        this.marketExternalContextWindowSummarySnapshotRepository = marketExternalContextWindowSummarySnapshotRepository;
    }

    @Transactional
    public List<MarketExternalContextWindowSummarySnapshotEntity> createAndSaveForReportType(
            MarketExternalContextSnapshotEntity currentSnapshot,
            AnalysisReportType reportType
    ) {
        List<MarketExternalContextWindowSummarySnapshotEntity> entities = new ArrayList<>();
        for (MarketWindowType windowType : windowTypes(reportType)) {
            entities.add(createAndSave(currentSnapshot, windowType));
        }
        return entities;
    }

    @Transactional
    public MarketExternalContextWindowSummarySnapshotEntity createAndSave(
            MarketExternalContextSnapshotEntity currentSnapshot,
            MarketWindowType windowType
    ) {
        MarketExternalContextWindowSummarySnapshot summary = marketExternalContextWindowSummarySnapshotService.create(
                currentSnapshot,
                windowType
        );

        MarketExternalContextWindowSummarySnapshotEntity existingEntity = marketExternalContextWindowSummarySnapshotRepository
                .findTopBySymbolAndWindowTypeAndWindowEndTimeOrderByIdDesc(
                        summary.symbol(),
                        summary.windowType().name(),
                        summary.windowEndTime()
                )
                .orElse(null);

        if (existingEntity == null) {
            MarketExternalContextWindowSummarySnapshotEntity entity = MarketExternalContextWindowSummarySnapshotEntity.builder()
                    .symbol(summary.symbol())
                    .windowType(summary.windowType().name())
                    .windowStartTime(summary.windowStartTime())
                    .windowEndTime(summary.windowEndTime())
                    .sampleCount(summary.sampleCount())
                    .currentCompositeRiskScore(summary.currentCompositeRiskScore())
                    .averageCompositeRiskScore(summary.averageCompositeRiskScore())
                    .currentCompositeRiskVsAverage(summary.currentCompositeRiskVsAverage())
                    .supportiveDominanceSampleCount(summary.supportiveDominanceSampleCount())
                    .cautionaryDominanceSampleCount(summary.cautionaryDominanceSampleCount())
                    .headwindDominanceSampleCount(summary.headwindDominanceSampleCount())
                    .highSeveritySampleCount(summary.highSeveritySampleCount())
                    .sourceDataVersion(summary.sourceDataVersion())
                    .build();
            return marketExternalContextWindowSummarySnapshotRepository.save(entity);
        }

        existingEntity.refreshFromSummary(
                summary.windowStartTime(),
                summary.sampleCount(),
                summary.currentCompositeRiskScore(),
                summary.averageCompositeRiskScore(),
                summary.currentCompositeRiskVsAverage(),
                summary.supportiveDominanceSampleCount(),
                summary.cautionaryDominanceSampleCount(),
                summary.headwindDominanceSampleCount(),
                summary.highSeveritySampleCount(),
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
