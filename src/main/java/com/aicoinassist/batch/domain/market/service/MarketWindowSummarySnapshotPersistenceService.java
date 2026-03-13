package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.Candle;
import com.aicoinassist.batch.domain.market.dto.MarketWindowSummarySnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.entity.MarketWindowSummarySnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.market.repository.MarketWindowSummarySnapshotRepository;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketWindowSummarySnapshotPersistenceService {

    private final MarketWindowSummarySnapshotService marketWindowSummarySnapshotService;
    private final MarketWindowSummarySnapshotRepository marketWindowSummarySnapshotRepository;

    @Transactional
    public List<MarketWindowSummarySnapshotEntity> createAndSaveForReportType(
            MarketIndicatorSnapshotEntity currentSnapshot,
            AnalysisReportType reportType,
            List<Candle> candles
    ) {
        List<MarketWindowSummarySnapshotEntity> entities = new ArrayList<>();
        for (MarketWindowType windowType : windowTypes(reportType)) {
            entities.add(createAndSave(currentSnapshot, windowType, candles));
        }
        return entities;
    }

    @Transactional
    public MarketWindowSummarySnapshotEntity createAndSave(
            MarketIndicatorSnapshotEntity currentSnapshot,
            MarketWindowType windowType,
            List<Candle> candles
    ) {
        MarketWindowSummarySnapshot summary = marketWindowSummarySnapshotService.create(currentSnapshot, windowType, candles);
        return persist(summary);
    }

    private MarketWindowSummarySnapshotEntity persist(MarketWindowSummarySnapshot summary) {
        MarketWindowSummarySnapshotEntity existingEntity = marketWindowSummarySnapshotRepository
                .findTopBySymbolAndIntervalValueAndWindowTypeAndWindowEndTimeOrderByIdDesc(
                        summary.symbol(),
                        summary.intervalValue(),
                        summary.windowType().name(),
                        summary.windowEndTime()
                )
                .orElse(null);

        if (existingEntity == null) {
            MarketWindowSummarySnapshotEntity entity = MarketWindowSummarySnapshotEntity.builder()
                                                                                        .symbol(summary.symbol())
                                                                                        .intervalValue(summary.intervalValue())
                                                                                        .windowType(summary.windowType().name())
                                                                                        .windowStartTime(summary.windowStartTime())
                                                                                        .windowEndTime(summary.windowEndTime())
                                                                                        .sampleCount(summary.sampleCount())
                                                                                        .currentPrice(summary.currentPrice())
                                                                                        .windowHigh(summary.windowHigh())
                                                                                        .windowLow(summary.windowLow())
                                                                                        .windowRange(summary.windowRange())
                                                                                        .currentPositionInRange(summary.currentPositionInRange())
                                                                                        .distanceFromWindowHigh(summary.distanceFromWindowHigh())
                                                                                        .reboundFromWindowLow(summary.reboundFromWindowLow())
                                                                                        .averageVolume(summary.averageVolume())
                                                                                        .averageQuoteAssetVolume(summary.averageQuoteAssetVolume())
                                                                                        .averageTradeCount(summary.averageTradeCount())
                                                                                        .averageAtr(summary.averageAtr())
                                                                                        .currentVolume(summary.currentVolume())
                                                                                        .currentQuoteAssetVolume(summary.currentQuoteAssetVolume())
                                                                                        .currentTradeCount(summary.currentTradeCount())
                                                                                        .currentAtr(summary.currentAtr())
                                                                                        .currentVolumeVsAverage(summary.currentVolumeVsAverage())
                                                                                        .currentQuoteAssetVolumeVsAverage(summary.currentQuoteAssetVolumeVsAverage())
                                                                                        .currentTradeCountVsAverage(summary.currentTradeCountVsAverage())
                                                                                        .currentTakerBuyQuoteRatio(summary.currentTakerBuyQuoteRatio())
                                                                                        .currentAtrVsAverage(summary.currentAtrVsAverage())
                                                                                        .sourceDataVersion(summary.sourceDataVersion())
                                                                                        .build();
            return marketWindowSummarySnapshotRepository.save(entity);
        }

        existingEntity.refreshFromSummary(
                summary.windowStartTime(),
                summary.sampleCount(),
                summary.currentPrice(),
                summary.windowHigh(),
                summary.windowLow(),
                summary.windowRange(),
                summary.currentPositionInRange(),
                summary.distanceFromWindowHigh(),
                summary.reboundFromWindowLow(),
                summary.averageVolume(),
                summary.averageQuoteAssetVolume(),
                summary.averageTradeCount(),
                summary.averageAtr(),
                summary.currentVolume(),
                summary.currentQuoteAssetVolume(),
                summary.currentTradeCount(),
                summary.currentAtr(),
                summary.currentVolumeVsAverage(),
                summary.currentQuoteAssetVolumeVsAverage(),
                summary.currentTradeCountVsAverage(),
                summary.currentTakerBuyQuoteRatio(),
                summary.currentAtrVsAverage(),
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
