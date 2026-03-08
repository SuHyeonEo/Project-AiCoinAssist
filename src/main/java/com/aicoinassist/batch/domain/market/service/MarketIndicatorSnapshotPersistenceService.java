package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.Candle;
import com.aicoinassist.batch.domain.market.dto.MarketIndicatorSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.repository.MarketIndicatorSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MarketIndicatorSnapshotPersistenceService {

    private final MarketIndicatorSnapshotService marketIndicatorSnapshotService;
    private final MarketIndicatorSnapshotRepository marketIndicatorSnapshotRepository;

    @Transactional
    public MarketIndicatorSnapshotEntity createAndSave(String symbol, CandleInterval interval) {
        MarketIndicatorSnapshot snapshot = marketIndicatorSnapshotService.create(symbol, interval);

        Candle latestCandle = snapshot.candles().get(snapshot.candles().size() - 1);
        String intervalValue = interval.value();

        MarketIndicatorSnapshotEntity existingEntity = marketIndicatorSnapshotRepository
                .findTopBySymbolAndIntervalValueAndSnapshotTimeOrderByIdDesc(
                        snapshot.symbol(),
                        intervalValue,
                        latestCandle.closeTime()
                )
                .orElse(null);

        String sourceDataVersion = buildSourceDataVersion(
                latestCandle.closeTime(),
                latestCandle.openTime(),
                snapshot.priceSnapshot().sourceEventTime()
        );

        if (existingEntity == null) {
            MarketIndicatorSnapshotEntity entity = MarketIndicatorSnapshotEntity.builder()
                                                                                .symbol(snapshot.symbol())
                                                                                .intervalValue(intervalValue)
                                                                                .snapshotTime(latestCandle.closeTime())
                                                                                .latestCandleOpenTime(latestCandle.openTime())
                                                                                .priceSourceEventTime(snapshot.priceSnapshot().sourceEventTime())
                                                                                .sourceDataVersion(sourceDataVersion)
                                                                                .currentPrice(snapshot.priceSnapshot().price())
                                                                                .ma20(snapshot.ma20().value())
                                                                                .ma60(snapshot.ma60().value())
                                                                                .ma120(snapshot.ma120().value())
                                                                                .rsi14(snapshot.rsi14().value())
                                                                                .macdLine(snapshot.macd().macdLine())
                                                                                .macdSignalLine(snapshot.macd().signalLine())
                                                                                .macdHistogram(snapshot.macd().histogram())
                                                                                .atr14(snapshot.atr14().value())
                                                                                .bollingerUpperBand(snapshot.bollingerBands20().upperBand())
                                                                                .bollingerMiddleBand(snapshot.bollingerBands20().middleBand())
                                                                                .bollingerLowerBand(snapshot.bollingerBands20().lowerBand())
                                                                                .build();

            return marketIndicatorSnapshotRepository.save(entity);
        }

        existingEntity.refreshFromSnapshot(
                latestCandle.openTime(),
                snapshot.priceSnapshot().sourceEventTime(),
                sourceDataVersion,
                snapshot.priceSnapshot().price(),
                snapshot.ma20().value(),
                snapshot.ma60().value(),
                snapshot.ma120().value(),
                snapshot.rsi14().value(),
                snapshot.macd().macdLine(),
                snapshot.macd().signalLine(),
                snapshot.macd().histogram(),
                snapshot.atr14().value(),
                snapshot.bollingerBands20().upperBand(),
                snapshot.bollingerBands20().middleBand(),
                snapshot.bollingerBands20().lowerBand()
        );

        return existingEntity;
    }

    private String buildSourceDataVersion(
            Instant snapshotTime,
            Instant latestCandleOpenTime,
            Instant priceSourceEventTime
    ) {
        return "snapshotTime=" + snapshotTime
                + ";latestCandleOpenTime=" + latestCandleOpenTime
                + ";priceSourceEventTime=" + priceSourceEventTime;
    }
}
