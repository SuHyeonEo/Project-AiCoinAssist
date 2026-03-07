package com.aicoinassist.batch.domain.indicator.calculator;

import com.aicoinassist.batch.domain.indicator.dto.BollingerBandsResult;
import com.aicoinassist.batch.domain.market.dto.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@Component
public class BollingerBandsCalculator {

    private static final int SCALE = 8;
    private static final MathContext MATH_CONTEXT = new MathContext(16, RoundingMode.HALF_UP);
    private static final BigDecimal STANDARD_DEVIATION_MULTIPLIER = BigDecimal.valueOf(2);

    public BollingerBandsResult calculate(List<Candle> candles, int period) {
        if (candles == null || candles.size() < period) {
            throw new IllegalArgumentException("볼린저 밴드 계산을 위한 캔들 데이터가 부족합니다.");
        }

        List<BigDecimal> closes = candles.subList(candles.size() - period, candles.size()).stream()
                                         .map(Candle::close)
                                         .toList();

        BigDecimal mean = closes.stream()
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);

        BigDecimal variance = closes.stream()
                                    .map(value -> value.subtract(mean, MATH_CONTEXT))
                                    .map(diff -> diff.multiply(diff, MATH_CONTEXT))
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                                    .divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);

        BigDecimal stdDev = variance.sqrt(MATH_CONTEXT).setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal upper = mean.add(stdDev.multiply(STANDARD_DEVIATION_MULTIPLIER, MATH_CONTEXT))
                               .setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal lower = mean.subtract(stdDev.multiply(STANDARD_DEVIATION_MULTIPLIER, MATH_CONTEXT))
                               .setScale(SCALE, RoundingMode.HALF_UP);

        return new BollingerBandsResult(period, upper, mean, lower);
    }
}