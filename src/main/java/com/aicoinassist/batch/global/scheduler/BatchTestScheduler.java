package com.aicoinassist.batch.global.scheduler;

import com.aicoinassist.batch.domain.market.dto.MarketPriceSnapshot;
import com.aicoinassist.batch.infrastructure.client.binance.BinanceMarketClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchTestScheduler {

    private final BinanceMarketClient binanceMarketClient;

    @Scheduled(fixedRate = 60000)
    public void run() {
        MarketPriceSnapshot snapshot = binanceMarketClient.getCurrentPrice("BTCUSDT");
        log.info("BTC 현재가 조회 성공 - symbol: {}, price: {}", snapshot.symbol(), snapshot.price());
    }
}