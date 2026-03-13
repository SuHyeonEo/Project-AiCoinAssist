package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.config.ExternalRawIngestionProperties;
import com.aicoinassist.batch.domain.market.dto.MarketPriceSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketPriceRawEntity;
import com.aicoinassist.batch.domain.market.enumtype.RawDataValidationStatus;
import com.aicoinassist.batch.domain.market.repository.MarketPriceRawRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MarketAnalysisPriceService {

    private final MarketPriceRawRepository marketPriceRawRepository;
    private final ExternalRawIngestionProperties externalRawIngestionProperties;
    private final Clock clock;

    public MarketPriceSnapshot getLatestAnalysisPrice(String symbol) {
        MarketPriceRawEntity latest = marketPriceRawRepository
                .findTopBySymbolAndValidationStatusOrderBySourceEventTimeDescIdDesc(
                        symbol,
                        RawDataValidationStatus.VALID
                )
                .orElseThrow(() -> new IllegalStateException("Analysis price raw data is missing for " + symbol));
        if (latest.getPrice() == null || latest.getSourceEventTime() == null) {
            throw new IllegalStateException("Analysis price raw data is incomplete for " + symbol);
        }

        Instant stalenessThreshold = clock.instant().minusMillis(externalRawIngestionProperties.marketPriceMaxStalenessMs());
        if (latest.getSourceEventTime().isBefore(stalenessThreshold)) {
            throw new IllegalStateException(
                    "Analysis price raw data is stale for %s: sourceEventTime=%s, maxStalenessMs=%s"
                            .formatted(symbol, latest.getSourceEventTime(), externalRawIngestionProperties.marketPriceMaxStalenessMs())
            );
        }

        return new MarketPriceSnapshot(symbol, latest.getPrice(), latest.getSourceEventTime());
    }
}
