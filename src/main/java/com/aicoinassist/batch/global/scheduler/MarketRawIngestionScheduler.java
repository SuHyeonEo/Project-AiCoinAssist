package com.aicoinassist.batch.global.scheduler;

import com.aicoinassist.batch.domain.market.config.MarketRawIngestionProperties;
import com.aicoinassist.batch.domain.market.dto.MarketRawIngestionResult;
import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.service.MarketRawIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "batch.scheduler.market-raw-ingestion",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MarketRawIngestionScheduler {

    private final MarketRawIngestionService marketRawIngestionService;
    private final MarketRawIngestionProperties marketRawIngestionProperties;

    @Scheduled(fixedDelayString = "${batch.scheduler.market-raw-ingestion.price-fixed-delay-ms:60000}")
    public void ingestPrices() {
        for (AssetType assetType : AssetType.values()) {
            try {
                RawDataValidationStatus validationStatus =
                        marketRawIngestionService.ingestPrice(assetType.symbol());

                log.info(
                        "price raw ingestion saved - symbol: {}, priceStatus: {}",
                        assetType.symbol(),
                        validationStatus
                );
            } catch (Exception exception) {
                log.error("price raw ingestion failed - symbol: {}", assetType.symbol(), exception);
            }
        }
    }

    @Scheduled(fixedDelayString = "${batch.scheduler.market-raw-ingestion.candle-fixed-delay-ms:600000}")
    public void ingestCandles() {
        for (AssetType assetType : AssetType.values()) {
            try {
                MarketRawIngestionResult result =
                        marketRawIngestionService.ingestCandles(
                                assetType.symbol(),
                                CandleInterval.ONE_HOUR,
                                marketRawIngestionProperties.candleLimit()
                        );

                log.info(
                        "candle raw ingestion saved - symbol: {}, interval: {}, candles: {}, invalidCandles: {}",
                        result.symbol(),
                        result.interval().value(),
                        result.candleCount(),
                        result.invalidCandleCount()
                );
            } catch (Exception exception) {
                log.error("candle raw ingestion failed - symbol: {}, interval: {}", assetType.symbol(), CandleInterval.ONE_HOUR.value(), exception);
            }
        }
    }
}
