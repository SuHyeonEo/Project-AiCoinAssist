package com.aicoinassist.batch.global.scheduler;

import com.aicoinassist.batch.domain.market.config.ExternalRawIngestionProperties;
import com.aicoinassist.batch.domain.market.dto.MarketCandleRawCoverageStatus;
import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.market.service.MarketCandleRawIngestionService;
import com.aicoinassist.batch.domain.market.service.MarketIndicatorSnapshotBackfillService;
import com.aicoinassist.batch.domain.report.config.AnalysisReportBatchProperties;
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
public class MarketCandleStartupBackfillRunner implements ApplicationRunner {

    private final MarketCandleRawIngestionService marketCandleRawIngestionService;
    private final MarketIndicatorSnapshotBackfillService marketIndicatorSnapshotBackfillService;
    private final ExternalRawIngestionProperties externalRawIngestionProperties;
    private final AnalysisReportBatchProperties analysisReportBatchProperties;

    @Override
    public void run(ApplicationArguments args) {
        if (!externalRawIngestionProperties.marketCandleEnabled()
                || !externalRawIngestionProperties.marketCandleStartupBackfillEnabled()) {
            return;
        }

        for (AssetType assetType : analysisReportBatchProperties.assetTypes()) {
            for (CandleInterval interval : analysisReportBatchProperties.snapshotIntervals(analysisReportBatchProperties.reportTypes())) {
                try {
                    MarketCandleRawCoverageStatus coverage = marketCandleRawIngestionService.startupBackfill(
                            assetType.symbol(),
                            interval,
                            interval.defaultBackfillLimit()
                    );
                    int rebuilt = marketIndicatorSnapshotBackfillService.rebuildFromRaw(assetType.symbol(), interval);
                    log.info(
                            "market candle startup backfill completed - symbol: {}, interval: {}, available: {}, missing: {}, tailGap: {}, rebuiltSnapshots: {}",
                            assetType.symbol(),
                            interval.value(),
                            coverage.availableValidCandleCount(),
                            coverage.missingCandleCount(),
                            coverage.tailGapCandleCount(),
                            rebuilt
                    );
                } catch (Exception exception) {
                    log.error("market candle startup backfill failed - symbol: {}, interval: {}", assetType.symbol(), interval.value(), exception);
                }
            }
        }
    }
}

