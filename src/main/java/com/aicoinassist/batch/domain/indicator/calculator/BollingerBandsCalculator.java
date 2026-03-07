package com.aicoinassist.batch.domain.indicator.calculator;

import com.aicoinassist.batch.domain.indicator.dto.BollingerBandsResult;
import com.aicoinassist.batch.domain.market.dto.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class BollingerBandsCalculator {

    public BollingerBandsResult calculate(List<Candle> candles, int period) {
        if (candles == null || candles.size() < period) {
            throw new IllegalArgumentException("볼린저 밴드 계산을 위한 캔들 데이터가 부족합니다.");
        }

        List<BigDecimal> closes = candles.subList(candles.size() - period, candles.size()).stream()
                                         .map(Candle::close)
                                         .toList();

        BigDecimal mean = closes.stream()
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);

        double variance = closes.stream()
                                .mapToDouble(value -> Math.pow(value.subtract(mean).doubleValue(), 2))
                                .average()
                                .orElse(0.0);

        double stdDev = Math.sqrt(variance);

        BigDecimal stdDevValue = BigDecimal.valueOf(stdDev);
        BigDecimal upper = mean.add(stdDevValue.multiply(BigDecimal.valueOf(2)));
        BigDecimal lower = mean.subtract(stdDevValue.multiply(BigDecimal.valueOf(2)));

        return new BollingerBandsResult(period, upper, mean, lower);
    }
}