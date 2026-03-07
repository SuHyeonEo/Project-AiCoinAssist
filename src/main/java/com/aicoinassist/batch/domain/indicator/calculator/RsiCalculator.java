package com.aicoinassist.batch.domain.indicator.calculator;

import com.aicoinassist.batch.domain.indicator.dto.RsiResult;
import com.aicoinassist.batch.domain.market.dto.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class RsiCalculator {

    public RsiResult calculate(List<Candle> candles, int period) {
        if (candles == null || candles.size() < period + 1) {
            throw new IllegalArgumentException("RSI 계산을 위한 캔들 데이터가 부족합니다.");
        }

        BigDecimal gainSum = BigDecimal.ZERO;
        BigDecimal lossSum = BigDecimal.ZERO;

        List<Candle> recentCandles = candles.subList(candles.size() - (period + 1), candles.size());

        for (int i = 1; i < recentCandles.size(); i++) {
            BigDecimal diff = recentCandles.get(i).close().subtract(recentCandles.get(i - 1).close());

            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                gainSum = gainSum.add(diff);
            } else {
                lossSum = lossSum.add(diff.abs());
            }
        }

        BigDecimal avgGain = gainSum.divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);
        BigDecimal avgLoss = lossSum.divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);

        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return new RsiResult(period, BigDecimal.valueOf(100));
        }

        BigDecimal rs = avgGain.divide(avgLoss, 8, RoundingMode.HALF_UP);
        BigDecimal rsi = BigDecimal.valueOf(100)
                                   .subtract(BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(rs), 8, RoundingMode.HALF_UP));

        return new RsiResult(period, rsi);
    }
}