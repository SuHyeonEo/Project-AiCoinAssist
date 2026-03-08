package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisReportPayload(
        String summary,
        String marketContext,
        List<AnalysisPriceLevel> supportLevels,
        List<AnalysisPriceLevel> resistanceLevels,
        List<AnalysisRiskFactor> riskFactors,
        List<AnalysisScenario> scenarios
) {
}
