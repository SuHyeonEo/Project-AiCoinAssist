package com.aicoinassist.batch.global.scheduler;

import com.aicoinassist.batch.domain.market.dto.Candle;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.infrastructure.client.binance.BinanceMarketClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchTestScheduler {

    private final BinanceMarketClient binanceMarketClient;

    @Scheduled(fixedRate = 60000)
    public void run() {
        List<Candle> candles = binanceMarketClient.getCandles("BTCUSDT", CandleInterval.ONE_HOUR, 5);

        Candle latest = candles.get(candles.size() - 1);

        log.info("BTC 최근 캔들 조회 성공 - closeTime: {}, close: {}, volume: {}",
                latest.closeTime(), latest.close(), latest.volume());
    }
}