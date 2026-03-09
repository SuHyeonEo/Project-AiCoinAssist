package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketContextWindowSummarySnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketContextWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.market.repository.MarketContextWindowSummarySnapshotRepository;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketContextWindowSummarySnapshotPersistenceService {

    private final MarketContextWindowSummarySnapshotService marketContextWindowSummarySnapshotService;
    private final MarketContextWindowSummarySnapshotRepository marketContextWindowSummarySnapshotRepository;

    @Transactional
    public List<MarketContextWindowSummarySnapshotEntity> createAndSaveForReportType(
            MarketContextSnapshotEntity currentSnapshot,
            AnalysisReportType reportType
    ) {
        List<MarketContextWindowSummarySnapshotEntity> entities = new ArrayList<>();
        for (MarketWindowType windowType : windowTypes(reportType)) {
            entities.add(createAndSave(currentSnapshot, windowType));
        }
        return entities;
    }

    @Transactional
    public MarketContextWindowSummarySnapshotEntity createAndSave(
            MarketContextSnapshotEntity currentSnapshot,
            MarketWindowType windowType
    ) {
        MarketContextWindowSummarySnapshot summary = marketContextWindowSummarySnapshotService.create(currentSnapshot, windowType);

        MarketContextWindowSummarySnapshotEntity existingEntity = marketContextWindowSummarySnapshotRepository
                .findTopBySymbolAndWindowTypeAndWindowEndTimeOrderByIdDesc(
                        summary.symbol(),
                        summary.windowType().name(),
                        summary.windowEndTime()
                )
                .orElse(null);

        if (existingEntity == null) {
            MarketContextWindowSummarySnapshotEntity entity = MarketContextWindowSummarySnapshotEntity.builder()
                                                                                                      .symbol(summary.symbol())
                                                                                                      .windowType(summary.windowType().name())
                                                                                                      .windowStartTime(summary.windowStartTime())
                                                                                                      .windowEndTime(summary.windowEndTime())
                                                                                                      .sampleCount(summary.sampleCount())
                                                                                                      .currentOpenInterest(summary.currentOpenInterest())
                                                                                                      .averageOpenInterest(summary.averageOpenInterest())
                                                                                                      .currentOpenInterestVsAverage(summary.currentOpenInterestVsAverage())
                                                                                                      .currentFundingRate(summary.currentFundingRate())
                                                                                                      .averageFundingRate(summary.averageFundingRate())
                                                                                                      .currentFundingVsAverage(summary.currentFundingVsAverage())
                                                                                                      .currentBasisRate(summary.currentBasisRate())
                                                                                                      .averageBasisRate(summary.averageBasisRate())
                                                                                                      .currentBasisVsAverage(summary.currentBasisVsAverage())
                                                                                                      .sourceDataVersion(summary.sourceDataVersion())
                                                                                                      .build();
            return marketContextWindowSummarySnapshotRepository.save(entity);
        }

        existingEntity.refreshFromSummary(
                summary.windowStartTime(),
                summary.sampleCount(),
                summary.currentOpenInterest(),
                summary.averageOpenInterest(),
                summary.currentOpenInterestVsAverage(),
                summary.currentFundingRate(),
                summary.averageFundingRate(),
                summary.currentFundingVsAverage(),
                summary.currentBasisRate(),
                summary.averageBasisRate(),
                summary.currentBasisVsAverage(),
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
