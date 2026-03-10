package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisLlmPromptInputPayload(
        AnalysisLlmPromptMetadata metadata,
        AnalysisLlmExecutiveSummary executiveSummary,
        List<AnalysisLlmDomainFactBlock> domainFactBlocks,
        List<AnalysisGptCrossSignal> crossSignals,
        AnalysisLlmRiskScenarioInput limitedRisksScenarios,
        List<AnalysisLlmReferenceNewsItem> optionalReferenceNews
) {
}
