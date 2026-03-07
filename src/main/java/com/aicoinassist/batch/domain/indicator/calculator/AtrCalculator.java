package com.aicoinassist.batch.domain.indicator.calculator;

import com.aicoinassist.batch.domain.indicator.dto.AtrResult;
import com.aicoinassist.batch.domain.market.dto.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class AtrCalculator {

    public AtrResult calculate(List<Candle> candles, int period) {
        if (candles == null || candles.size() < period + 1) {
            throw new IllegalArgumentException("ATR 계산을 위한 캔들 데이터가 부족합니다.");
        }

        List<BigDecimal> trueRanges = new ArrayList<>();

        for (int i = 1; i < candles.size(); i++) {
            Candle current = candles.get(i);
            Candle previous = candles.get(i - 1);

            BigDecimal highLow = current.high().subtract(current.low()).abs();
            BigDecimal highClose = current.high().subtract(previous.close()).abs();
            BigDecimal lowClose = current.low().subtract(previous.close()).abs();

            BigDecimal tr = highLow.max(highClose).max(lowClose);
            trueRanges.add(tr);
        }

        List<BigDecimal> recentTr = trueRanges.subList(trueRanges.size() - period, trueRanges.size());

        BigDecimal atr = recentTr.stream()
                                 .reduce(BigDecimal.ZERO, BigDecimal::add)
                                 .divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);

        return new AtrResult(period, atr);
    }
}