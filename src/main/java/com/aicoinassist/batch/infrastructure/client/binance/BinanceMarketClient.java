package com.aicoinassist.batch.infrastructure.client.binance;

import com.aicoinassist.batch.domain.market.dto.MarketPriceSnapshot;
import com.aicoinassist.batch.infrastructure.client.binance.dto.BinanceTickerPriceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class BinanceMarketClient {

    private final BinanceApiClient binanceApiClient;

    public MarketPriceSnapshot getCurrentPrice(String symbol) {
        BinanceTickerPriceResponse response = binanceApiClient.getTickerPrice(symbol);

        return new MarketPriceSnapshot(
                response.symbol(),
                new BigDecimal(response.price())
        );
    }
}