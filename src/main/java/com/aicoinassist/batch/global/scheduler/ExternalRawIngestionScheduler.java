package com.aicoinassist.batch.global.scheduler;

import com.aicoinassist.batch.domain.macro.service.MacroRawIngestionService;
import com.aicoinassist.batch.domain.market.config.ExternalRawIngestionProperties;
import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.service.MarketPriceRawIngestionService;
import com.aicoinassist.batch.domain.onchain.service.OnchainRawIngestionService;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchProperties;
import com.aicoinassist.batch.domain.sentiment.service.SentimentRawIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "batch.scheduler.external-raw-ingestion",
        name = "enabled",
        havingValue = "true"
)
public class ExternalRawIngestionScheduler {

    private final MacroRawIngestionService macroRawIngestionService;
    private final SentimentRawIngestionService sentimentRawIngestionService;
    private final OnchainRawIngestionService onchainRawIngestionService;
    private final MarketPriceRawIngestionService marketPriceRawIngestionService;
    private final ExternalRawIngestionProperties externalRawIngestionProperties;
    private final AnalysisReportBatchProperties analysisReportBatchProperties;

    @Scheduled(
            fixedDelayString = "${batch.scheduler.external-raw-ingestion.market-price-fixed-delay-ms:60000}",
            initialDelayString = "${batch.scheduler.external-raw-ingestion.market-price-initial-delay-ms:60000}"
    )
    public void ingestMarketPrice() {
        if (!externalRawIngestionProperties.marketPriceEnabled()) {
            return;
        }

        for (AssetType assetType : analysisReportBatchProperties.assetTypes()) {
            try {
                marketPriceRawIngestionService.ingestLatestPrice(assetType.symbol());
                log.info("market price raw ingestion saved - symbol: {}", assetType.symbol());
            } catch (Exception exception) {
                log.error("market price raw ingestion failed - symbol: {}", assetType.symbol(), exception);
            }
        }
    }

    @Scheduled(
            fixedDelayString = "${batch.scheduler.external-raw-ingestion.macro-fixed-delay-ms:3600000}",
            initialDelayString = "${batch.scheduler.external-raw-ingestion.macro-initial-delay-ms:60000}"
    )
    public void ingestMacro() {
        if (!externalRawIngestionProperties.macroEnabled()) {
            return;
        }

        try {
            macroRawIngestionService.ingestAll();
            log.info("macro raw ingestion saved - metrics: {}", 3);
        } catch (Exception exception) {
            log.error("macro raw ingestion failed", exception);
        }
    }

    @Scheduled(
            fixedDelayString = "${batch.scheduler.external-raw-ingestion.sentiment-fixed-delay-ms:21600000}",
            initialDelayString = "${batch.scheduler.external-raw-ingestion.sentiment-initial-delay-ms:60000}"
    )
    public void ingestSentiment() {
        if (!externalRawIngestionProperties.sentimentEnabled()) {
            return;
        }

        try {
            sentimentRawIngestionService.ingestFearGreed();
            log.info("sentiment raw ingestion saved - metric: FEAR_GREED_INDEX");
        } catch (Exception exception) {
            log.error("sentiment raw ingestion failed - metric: FEAR_GREED_INDEX", exception);
        }
    }

    @Scheduled(
            fixedDelayString = "${batch.scheduler.external-raw-ingestion.onchain-fixed-delay-ms:43200000}",
            initialDelayString = "${batch.scheduler.external-raw-ingestion.onchain-initial-delay-ms:60000}"
    )
    public void ingestOnchain() {
        if (!externalRawIngestionProperties.onchainEnabled()) {
            return;
        }

        for (AssetType assetType : analysisReportBatchProperties.assetTypes()) {
            try {
                onchainRawIngestionService.ingestAll(assetType);
                log.info("onchain raw ingestion saved - symbol: {}", assetType.symbol());
            } catch (Exception exception) {
                log.error("onchain raw ingestion failed - symbol: {}", assetType.symbol(), exception);
            }
        }
    }
}
