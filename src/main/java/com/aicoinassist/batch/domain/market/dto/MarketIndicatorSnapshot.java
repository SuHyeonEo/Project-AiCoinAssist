package com.aicoinassist.batch.domain.market.dto;

import com.aicoinassist.batch.domain.indicator.dto.AtrResult;
import com.aicoinassist.batch.domain.indicator.dto.BollingerBandsResult;
import com.aicoinassist.batch.domain.indicator.dto.MacdResult;
import com.aicoinassist.batch.domain.indicator.dto.MovingAverageResult;
import com.aicoinassist.batch.domain.indicator.dto.RsiResult;

import java.util.List;

public record MarketIndicatorSnapshot(
        String symbol,
        MarketPriceSnapshot priceSnapshot,
        List<Candle> candles,
        MovingAverageResult ma20,
        MovingAverageResult ma60,
        MovingAverageResult ma120,
        RsiResult rsi14,
        MacdResult macd,
        AtrResult atr14,
        BollingerBandsResult bollingerBands20
) {
}