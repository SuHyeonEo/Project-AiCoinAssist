package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisRangePositionLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisTrendLabel;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisVolatilityLabel;

import java.math.BigDecimal;
import java.util.List;

public record AnalysisCurrentStatePayload(
        BigDecimal currentPrice,
        AnalysisTrendLabel trendLabel,
        AnalysisVolatilityLabel volatilityLabel,
        AnalysisRangePositionLabel rangePositionLabel,
        List<AnalysisMovingAveragePositionPayload> movingAveragePositions,
        AnalysisMomentumStatePayload momentumState
) {
}
