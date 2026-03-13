package com.aicoinassist.batch.global.scheduler;

import com.aicoinassist.batch.domain.market.config.ExternalRawIngestionProperties;
import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.service.MarketPriceRawIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "batch.scheduler.external-raw-ingestion",
        name = "enabled",
        havingValue = "true"
)
public class MarketPriceStartupWarmupRunner implements ApplicationRunner {

    private final MarketPriceRawIngestionService marketPriceRawIngestionService;
    private final ExternalRawIngestionProperties externalRawIngestionProperties;

    @Override
    public void run(ApplicationArguments args) {
        if (!externalRawIngestionProperties.marketPriceEnabled()) {
            return;
        }

        for (AssetType assetType : AssetType.values()) {
            try {
                marketPriceRawIngestionService.ingestLatestPrice(assetType.symbol());
                log.info("market price startup warmup completed - symbol: {}", assetType.symbol());
            } catch (Exception exception) {
                log.error("market price startup warmup failed - symbol: {}", assetType.symbol(), exception);
            }
        }
    }
}
