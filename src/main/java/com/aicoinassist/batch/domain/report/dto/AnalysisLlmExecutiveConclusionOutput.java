package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisLlmExecutiveConclusionOutput(
        String overallTone,
        List<String> topSupportingFactors,
        List<String> topRiskFactors,
        String summary
) {
}
