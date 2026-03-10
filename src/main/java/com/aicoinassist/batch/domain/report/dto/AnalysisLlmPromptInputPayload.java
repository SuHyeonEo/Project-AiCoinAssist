package com.aicoinassist.batch.domain.report.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnalysisLlmPromptInputPayload(
        AnalysisLlmPromptMetadata metadata,
        AnalysisLlmExecutiveSummary executiveSummary,
        List<AnalysisLlmDomainFactBlock> domainFactBlocks,
        List<AnalysisGptCrossSignal> crossSignals,
        AnalysisLlmRiskScenarioInput limitedRisksScenarios,
        List<AnalysisLlmReferenceNewsItem> optionalReferenceNews
) {
}
