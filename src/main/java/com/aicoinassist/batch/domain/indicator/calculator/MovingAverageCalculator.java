package com.aicoinassist.batch.domain.indicator.calculator;

import com.aicoinassist.batch.domain.indicator.dto.MovingAverageResult;
import com.aicoinassist.batch.domain.market.dto.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class MovingAverageCalculator {

    public MovingAverageResult calculate(List<Candle> candles, int period) {
        validate(candles, period);

        BigDecimal sum = candles.subList(candles.size() - period, candles.size()).stream()
                                .map(Candle::close)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = sum.divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);

        return new MovingAverageResult(period, average);
    }

    private void validate(List<Candle> candles, int period) {
        if (candles == null || candles.size() < period) {
            throw new IllegalArgumentException("이동평균 계산을 위한 캔들 데이터가 부족합니다.");
        }
    }
}