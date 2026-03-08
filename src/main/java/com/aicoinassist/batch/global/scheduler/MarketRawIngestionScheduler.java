package com.aicoinassist.batch.global.scheduler;

import com.aicoinassist.batch.domain.market.dto.MarketRawIngestionResult;
import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.service.MarketRawIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketRawIngestionScheduler {

    private final MarketRawIngestionService marketRawIngestionService;

    @Scheduled(fixedDelay = 60000)
    public void run() {
        for (AssetType assetType : AssetType.values()) {
            try {
                MarketRawIngestionResult result =
                        marketRawIngestionService.ingest(assetType.symbol(), CandleInterval.ONE_HOUR);

                log.info(
                        "raw ingestion saved - symbol: {}, interval: {}, candles: {}, invalidCandles: {}, priceStatus: {}",
                        result.symbol(),
                        result.interval().value(),
                        result.candleCount(),
                        result.invalidCandleCount(),
                        result.priceValidationStatus()
                );
            } catch (Exception exception) {
                log.error("raw ingestion failed - symbol: {}, interval: {}", assetType.symbol(), CandleInterval.ONE_HOUR.value(), exception);
            }
        }
    }
}
