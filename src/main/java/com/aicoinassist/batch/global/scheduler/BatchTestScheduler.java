package com.aicoinassist.batch.global.scheduler;

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
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.infrastructure.client.binance.BinanceMarketClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchTestScheduler {

    private final BinanceMarketClient binanceMarketClient;
    private final MovingAverageCalculator movingAverageCalculator;
    private final RsiCalculator rsiCalculator;
    private final MacdCalculator macdCalculator;
    private final AtrCalculator atrCalculator;
    private final BollingerBandsCalculator bollingerBandsCalculator;

    @Scheduled(fixedRate = 60000)
    public void run() {
        List<Candle> candles = binanceMarketClient.getCandles("BTCUSDT", CandleInterval.ONE_HOUR, 120);

        MovingAverageResult ma20 = movingAverageCalculator.calculate(candles, 20);
        RsiResult rsi14 = rsiCalculator.calculate(candles, 14);
        MacdResult macd = macdCalculator.calculate(candles);
        AtrResult atr14 = atrCalculator.calculate(candles, 14);
        BollingerBandsResult bollinger = bollingerBandsCalculator.calculate(candles, 20);

        log.info("MA20: {}", ma20.value());
        log.info("RSI14: {}", rsi14.value());
        log.info("MACD: {}, Signal: {}, Hist: {}", macd.macdLine(), macd.signalLine(), macd.histogram());
        log.info("ATR14: {}", atr14.value());
        log.info("BB Upper: {}, Middle: {}, Lower: {}", bollinger.upperBand(), bollinger.middleBand(), bollinger.lowerBand());
    }
}