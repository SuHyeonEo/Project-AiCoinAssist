package com.aicoinassist.batch.global.scheduler;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.service.MarketIndicatorSnapshotPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchTestScheduler {

    private final MarketIndicatorSnapshotPersistenceService marketIndicatorSnapshotPersistenceService;

    @Scheduled(fixedRate = 60000)
    public void run() {
        MarketIndicatorSnapshotEntity saved =
                marketIndicatorSnapshotPersistenceService.createAndSave("BTCUSDT", CandleInterval.ONE_HOUR);

        log.info(
                "snapshot saved - id: {}, symbol: {}, snapshotTime: {}, price: {}, rsi14: {}",
                saved.getId(),
                saved.getSymbol(),
                saved.getSnapshotTime(),
                saved.getCurrentPrice(),
                saved.getRsi14()
        );
    }
}