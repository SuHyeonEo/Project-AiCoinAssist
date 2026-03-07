package com.aicoinassist.batch.domain.indicator.calculator;

import com.aicoinassist.batch.domain.indicator.dto.MacdResult;
import com.aicoinassist.batch.domain.market.dto.Candle;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class MacdCalculator {

    public MacdResult calculate(List<Candle> candles) {
        if (candles == null || candles.size() < 35) {
            throw new IllegalArgumentException("MACD 계산을 위한 캔들 데이터가 부족합니다.");
        }

        List<BigDecimal> closes = candles.stream()
                                         .map(Candle::close)
                                         .toList();

        List<BigDecimal> ema12 = calculateEmaSeries(closes, 12);
        List<BigDecimal> ema26 = calculateEmaSeries(closes, 26);

        int startIndex = closes.size() - ema26.size();
        List<BigDecimal> alignedEma12 = ema12.subList(ema12.size() - ema26.size(), ema12.size());

        List<BigDecimal> macdSeries = new ArrayList<>();
        for (int i = 0; i < ema26.size(); i++) {
            macdSeries.add(alignedEma12.get(i).subtract(ema26.get(i)));
        }

        List<BigDecimal> signalSeries = calculateEmaSeries(macdSeries, 9);

        BigDecimal macdLine = macdSeries.get(macdSeries.size() - 1);
        BigDecimal signalLine = signalSeries.get(signalSeries.size() - 1);
        BigDecimal histogram = macdLine.subtract(signalLine);

        return new MacdResult(macdLine, signalLine, histogram);
    }

    private List<BigDecimal> calculateEmaSeries(List<BigDecimal> values, int period) {
        if (values.size() < period) {
            throw new IllegalArgumentException("EMA 계산 데이터가 부족합니다.");
        }

        List<BigDecimal> emaSeries = new ArrayList<>();
        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (period + 1));

        BigDecimal sma = values.subList(0, period).stream()
                               .reduce(BigDecimal.ZERO, BigDecimal::add)
                               .divide(BigDecimal.valueOf(period), 8, RoundingMode.HALF_UP);

        emaSeries.add(sma);

        BigDecimal prevEma = sma;
        for (int i = period; i < values.size(); i++) {
            BigDecimal ema = values.get(i).subtract(prevEma)
                                   .multiply(multiplier)
                                   .add(prevEma)
                                   .setScale(8, RoundingMode.HALF_UP);

            emaSeries.add(ema);
            prevEma = ema;
        }

        return emaSeries;
    }
}