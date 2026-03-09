package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketDerivativeRawIngestionResult;
import com.aicoinassist.batch.domain.market.dto.MarketDerivativeSnapshot;
import com.aicoinassist.batch.domain.market.entity.MarketOpenInterestRawEntity;
import com.aicoinassist.batch.domain.market.entity.MarketPremiumIndexRawEntity;
import com.aicoinassist.batch.domain.market.repository.MarketOpenInterestRawRepository;
import com.aicoinassist.batch.domain.market.repository.MarketPremiumIndexRawRepository;
import com.aicoinassist.batch.infrastructure.client.binance.BinanceDerivativesClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MarketDerivativeRawIngestionService {

    private static final String BINANCE_SOURCE = "BINANCE";

    private final BinanceDerivativesClient binanceDerivativesClient;
    private final MarketOpenInterestRawRepository marketOpenInterestRawRepository;
    private final MarketPremiumIndexRawRepository marketPremiumIndexRawRepository;

    @Transactional
    public MarketDerivativeRawIngestionResult ingest(String symbol) {
        Instant collectedTime = Instant.now();
        MarketDerivativeSnapshot snapshot = binanceDerivativesClient.fetchSnapshot(symbol);

        persistOrRefreshOpenInterest(snapshot, collectedTime);
        persistOrRefreshPremiumIndex(snapshot, collectedTime);

        return new MarketDerivativeRawIngestionResult(
                symbol,
                collectedTime,
                snapshot.openInterestValidation().status(),
                snapshot.premiumIndexValidation().status()
        );
    }

    private void persistOrRefreshOpenInterest(MarketDerivativeSnapshot snapshot, Instant collectedTime) {
        MarketOpenInterestRawEntity existingEntity = snapshot.openInterestSourceEventTime() == null
                ? null
                : marketOpenInterestRawRepository
                        .findTopBySourceAndSymbolAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
                                BINANCE_SOURCE,
                                snapshot.symbol(),
                                snapshot.openInterestSourceEventTime()
                        )
                        .orElse(null);

        if (existingEntity == null) {
            marketOpenInterestRawRepository.save(
                    MarketOpenInterestRawEntity.builder()
                                               .source(BINANCE_SOURCE)
                                               .symbol(snapshot.symbol())
                                               .sourceEventTime(snapshot.openInterestSourceEventTime())
                                               .collectedTime(collectedTime)
                                               .validationStatus(snapshot.openInterestValidation().status())
                                               .validationDetails(snapshot.openInterestValidation().details())
                                               .openInterest(snapshot.openInterest())
                                               .rawPayload(snapshot.openInterestRawPayload())
                                               .build()
            );
            return;
        }

        existingEntity.refreshFromIngestion(
                collectedTime,
                snapshot.openInterestValidation().status(),
                snapshot.openInterestValidation().details(),
                snapshot.openInterest(),
                snapshot.openInterestRawPayload()
        );
    }

    private void persistOrRefreshPremiumIndex(MarketDerivativeSnapshot snapshot, Instant collectedTime) {
        MarketPremiumIndexRawEntity existingEntity = snapshot.premiumIndexSourceEventTime() == null
                ? null
                : marketPremiumIndexRawRepository
                        .findTopBySourceAndSymbolAndSourceEventTimeOrderByCollectedTimeDescIdDesc(
                                BINANCE_SOURCE,
                                snapshot.symbol(),
                                snapshot.premiumIndexSourceEventTime()
                        )
                        .orElse(null);

        if (existingEntity == null) {
            marketPremiumIndexRawRepository.save(
                    MarketPremiumIndexRawEntity.builder()
                                               .source(BINANCE_SOURCE)
                                               .symbol(snapshot.symbol())
                                               .sourceEventTime(snapshot.premiumIndexSourceEventTime())
                                               .collectedTime(collectedTime)
                                               .validationStatus(snapshot.premiumIndexValidation().status())
                                               .validationDetails(snapshot.premiumIndexValidation().details())
                                               .markPrice(snapshot.markPrice())
                                               .indexPrice(snapshot.indexPrice())
                                               .lastFundingRate(snapshot.lastFundingRate())
                                               .nextFundingTime(snapshot.nextFundingTime())
                                               .rawPayload(snapshot.premiumIndexRawPayload())
                                               .build()
            );
            return;
        }

        existingEntity.refreshFromIngestion(
                collectedTime,
                snapshot.premiumIndexValidation().status(),
                snapshot.premiumIndexValidation().details(),
                snapshot.markPrice(),
                snapshot.indexPrice(),
                snapshot.lastFundingRate(),
                snapshot.nextFundingTime(),
                snapshot.premiumIndexRawPayload()
        );
    }
}
