package com.aicoinassist.batch.domain.indicator.calculator;

import com.aicoinassist.batch.domain.indicator.dto.RsiResult;
import com.aicoinassist.batch.domain.market.dto.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class RsiCalculator {

    private static final int SCALE = 8;

    public RsiResult calculate(List<Candle> candles, int period) {
        if (candles == null || candles.size() < period + 1) {
            throw new IllegalArgumentException("RSI 계산을 위한 캔들 데이터가 부족합니다.");
        }

        List<Candle> recentCandles = candles;
        BigDecimal avgGain = BigDecimal.ZERO;
        BigDecimal avgLoss = BigDecimal.ZERO;

        for (int i = 1; i <= period; i++) {
            BigDecimal diff = recentCandles.get(i).close().subtract(recentCandles.get(i - 1).close());

            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                avgGain = avgGain.add(diff);
            } else {
                avgLoss = avgLoss.add(diff.abs());
            }
        }

        avgGain = avgGain.divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);
        avgLoss = avgLoss.divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);

        for (int i = period + 1; i < recentCandles.size(); i++) {
            BigDecimal diff = recentCandles.get(i).close().subtract(recentCandles.get(i - 1).close());

            BigDecimal gain = diff.compareTo(BigDecimal.ZERO) > 0 ? diff : BigDecimal.ZERO;
            BigDecimal loss = diff.compareTo(BigDecimal.ZERO) < 0 ? diff.abs() : BigDecimal.ZERO;

            avgGain = avgGain.multiply(BigDecimal.valueOf(period - 1))
                             .add(gain)
                             .divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);

            avgLoss = avgLoss.multiply(BigDecimal.valueOf(period - 1))
                             .add(loss)
                             .divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);
        }

        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return new RsiResult(period, BigDecimal.valueOf(100));
        }

        BigDecimal rs = avgGain.divide(avgLoss, SCALE, RoundingMode.HALF_UP);
        BigDecimal rsi = BigDecimal.valueOf(100)
                                   .subtract(BigDecimal.valueOf(100).divide(BigDecimal.ONE.add(rs), SCALE, RoundingMode.HALF_UP));

        return new RsiResult(period, rsi);
    }
}