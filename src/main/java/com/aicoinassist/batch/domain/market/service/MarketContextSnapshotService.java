package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketContextSnapshot;
import com.aicoinassist.batch.domain.market.dto.MarketDerivativeSnapshot;
import com.aicoinassist.batch.infrastructure.client.binance.BinanceDerivativesClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class MarketContextSnapshotService {

    private final BinanceDerivativesClient binanceDerivativesClient;

    public MarketContextSnapshot create(String symbol) {
        MarketDerivativeSnapshot snapshot = binanceDerivativesClient.fetchSnapshot(symbol);

        if (!snapshot.openInterestValidation().isValid()) {
            throw new IllegalStateException("Open interest snapshot is invalid: " + snapshot.openInterestValidation().details());
        }

        if (!snapshot.premiumIndexValidation().isValid()) {
            throw new IllegalStateException("Premium index snapshot is invalid: " + snapshot.premiumIndexValidation().details());
        }

        Instant snapshotTime = snapshot.premiumIndexSourceEventTime().isAfter(snapshot.openInterestSourceEventTime())
                ? snapshot.premiumIndexSourceEventTime()
                : snapshot.openInterestSourceEventTime();

        return new MarketContextSnapshot(
                symbol,
                snapshotTime,
                snapshot.openInterestSourceEventTime(),
                snapshot.premiumIndexSourceEventTime(),
                buildSourceDataVersion(snapshot),
                snapshot.openInterest(),
                snapshot.markPrice(),
                snapshot.indexPrice(),
                snapshot.lastFundingRate(),
                snapshot.nextFundingTime(),
                markIndexBasisRate(snapshot.markPrice(), snapshot.indexPrice())
        );
    }

    private String buildSourceDataVersion(MarketDerivativeSnapshot snapshot) {
        return "openInterestSourceEventTime=" + snapshot.openInterestSourceEventTime()
                + ";premiumIndexSourceEventTime=" + snapshot.premiumIndexSourceEventTime()
                + ";nextFundingTime=" + snapshot.nextFundingTime();
    }

    private BigDecimal markIndexBasisRate(BigDecimal markPrice, BigDecimal indexPrice) {
        return markPrice.subtract(indexPrice)
                        .multiply(new BigDecimal("100"))
                        .divide(indexPrice, 8, RoundingMode.HALF_UP);
    }
}
