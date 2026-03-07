package com.aicoinassist.batch.global.scheduler;

import com.aicoinassist.batch.domain.market.dto.MarketIndicatorSnapshot;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.service.MarketIndicatorSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchTestScheduler {

    private final MarketIndicatorSnapshotService marketIndicatorSnapshotService;

    @Scheduled(fixedRate = 60000)
    public void run() {
        MarketIndicatorSnapshot snapshot =
                marketIndicatorSnapshotService.create("BTCUSDT", CandleInterval.ONE_HOUR);

        log.info(
                "snapshot created - symbol: {}, price: {}, ma20: {}, rsi14: {}, macdHist: {}, atr14: {}, bbUpper: {}",
                snapshot.symbol(),
                snapshot.priceSnapshot().price(),
                snapshot.ma20().value(),
                snapshot.rsi14().value(),
                snapshot.macd().histogram(),
                snapshot.atr14().value(),
                snapshot.bollingerBands20().upperBand()
        );
    }
}