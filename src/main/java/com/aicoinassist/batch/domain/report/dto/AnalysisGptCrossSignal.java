package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisGptCrossSignalCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisGptSignalBias;

import java.util.List;

public record AnalysisGptCrossSignal(
        AnalysisGptCrossSignalCategory category,
        String title,
        AnalysisGptSignalBias bias,
        int strengthScore,
        List<String> supportingFacts,
        String summary
) {
}
