package com.aicoinassist.batch.domain.market.service;

import com.aicoinassist.batch.domain.indicator.calculator.AtrCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.BollingerBandsCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.MacdCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.MovingAverageCalculator;
import com.aicoinassist.batch.domain.indicator.calculator.RsiCalculator;
import com.aicoinassist.batch.domain.indicator.dto.AtrResult;
import com.aicoinassist.batch.domain.indicator.dto.BollingerBandsResult;
import com.aicoinassist.batch.domain.indicator.dto.MacdResult;
import com.aicoinassist.batch.domain.indicator.dto.MovingAverageResult;
import com.aicoinassist.batch.domain.indicator.dto.RsiResult;
import com.aicoinassist.batch.domain.market.dto.Candle;
import com.aicoinassist.batch.domain.market.dto.MarketIndicatorSnapshot;
import com.aicoinassist.batch.domain.market.dto.MarketPriceSnapshot;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.infrastructure.client.binance.BinanceMarketClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketIndicatorSnapshotService {

    private static final int DEFAULT_CANDLE_LIMIT = 120;

    private final BinanceMarketClient binanceMarketClient;
    private final MovingAverageCalculator movingAverageCalculator;
    private final RsiCalculator rsiCalculator;
    private final MacdCalculator macdCalculator;
    private final AtrCalculator atrCalculator;
    private final BollingerBandsCalculator bollingerBandsCalculator;

    public MarketIndicatorSnapshot create(String symbol, CandleInterval interval) {
        return create(symbol, interval, DEFAULT_CANDLE_LIMIT);
    }

    public MarketIndicatorSnapshot create(String symbol, CandleInterval interval, int candleLimit) {
        MarketPriceSnapshot priceSnapshot = binanceMarketClient.getCurrentPrice(symbol);
        List<Candle> candles = binanceMarketClient.getClosedCandles(symbol, interval, candleLimit);

        MovingAverageResult ma20 = movingAverageCalculator.calculate(candles, 20);
        MovingAverageResult ma60 = movingAverageCalculator.calculate(candles, 60);
        MovingAverageResult ma120 = movingAverageCalculator.calculate(candles, 120);
        RsiResult rsi14 = rsiCalculator.calculate(candles, 14);
        MacdResult macd = macdCalculator.calculate(candles);
        AtrResult atr14 = atrCalculator.calculate(candles, 14);
        BollingerBandsResult bollingerBands20 = bollingerBandsCalculator.calculate(candles, 20);

        return new MarketIndicatorSnapshot(
                symbol,
                priceSnapshot,
                candles,
                ma20,
                ma60,
                ma120,
                rsi14,
                macd,
                atr14,
                bollingerBands20
        );
    }
}
