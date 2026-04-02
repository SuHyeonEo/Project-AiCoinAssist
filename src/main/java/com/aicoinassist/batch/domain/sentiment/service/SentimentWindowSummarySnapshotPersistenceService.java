package com.aicoinassist.batch.domain.sentiment.service;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.sentiment.dto.SentimentWindowSummarySnapshot;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentSnapshotEntity;
import com.aicoinassist.batch.domain.sentiment.entity.SentimentWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.sentiment.repository.SentimentWindowSummarySnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SentimentWindowSummarySnapshotPersistenceService {

    private final SentimentWindowSummarySnapshotService sentimentWindowSummarySnapshotService;
    private final SentimentWindowSummarySnapshotRepository sentimentWindowSummarySnapshotRepository;

    @Transactional
    public List<SentimentWindowSummarySnapshotEntity> createAndSaveForReportType(
            SentimentSnapshotEntity currentSnapshot,
            AnalysisReportType reportType
    ) {
        List<SentimentWindowSummarySnapshotEntity> entities = new ArrayList<>();
        for (MarketWindowType windowType : windowTypes(reportType)) {
            entities.add(createAndSave(currentSnapshot, windowType));
        }
        return entities;
    }

    @Transactional
    public SentimentWindowSummarySnapshotEntity createAndSave(
            SentimentSnapshotEntity currentSnapshot,
            MarketWindowType windowType
    ) {
        SentimentWindowSummarySnapshot summary = sentimentWindowSummarySnapshotService.create(currentSnapshot, windowType);

        SentimentWindowSummarySnapshotEntity existingEntity = sentimentWindowSummarySnapshotRepository
                .findTopByMetricTypeAndWindowTypeAndWindowEndTimeOrderByIdDesc(
                        summary.metricType(),
                        summary.windowType().name(),
                        summary.windowEndTime()
                )
                .orElse(null);

        if (existingEntity == null) {
            SentimentWindowSummarySnapshotEntity entity = SentimentWindowSummarySnapshotEntity.builder()
                                                                                              .metricType(summary.metricType())
                                                                                              .windowType(summary.windowType().name())
                                                                                              .windowStartTime(summary.windowStartTime())
                                                                                              .windowEndTime(summary.windowEndTime())
                                                                                              .sampleCount(summary.sampleCount())
                                                                                              .currentIndexValue(summary.currentIndexValue())
                                                                                              .averageIndexValue(summary.averageIndexValue())
                                                                                              .currentIndexVsAverage(summary.currentIndexVsAverage())
                                                                                              .currentClassification(summary.currentClassification())
                                                                                              .greedSampleCount(summary.greedSampleCount())
                                                                                              .fearSampleCount(summary.fearSampleCount())
                                                                                              .sourceDataVersion(summary.sourceDataVersion())
                                                                                              .build();
            return sentimentWindowSummarySnapshotRepository.save(entity);
        }

        existingEntity.refreshFromSummary(
                summary.windowStartTime(),
                summary.sampleCount(),
                summary.currentIndexValue(),
                summary.averageIndexValue(),
                summary.currentIndexVsAverage(),
                summary.currentClassification(),
                summary.greedSampleCount(),
                summary.fearSampleCount(),
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
