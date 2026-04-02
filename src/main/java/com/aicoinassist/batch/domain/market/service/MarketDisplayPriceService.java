package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.market.dto.MarketPriceSnapshot;
import com.aicoinassist.batch.infrastructure.client.binance.BinanceMarketClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarketDisplayPriceService {

    private final BinanceMarketClient binanceMarketClient;

    public MarketPriceSnapshot getLiveDisplayPrice(String symbol) {
        return binanceMarketClient.getCurrentPrice(symbol);
    }
}
