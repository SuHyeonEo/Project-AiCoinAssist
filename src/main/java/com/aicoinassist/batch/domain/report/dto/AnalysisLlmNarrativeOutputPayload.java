package com.aicoinassist.batch.domain.report.dto;

import java.util.List;

public record AnalysisLlmNarrativeOutputPayload(
        AnalysisLlmExecutiveConclusionOutput executiveConclusion,
        List<AnalysisLlmDomainAnalysisOutput> domainAnalyses,
        AnalysisLlmCrossSignalIntegrationOutput crossSignalIntegration,
        List<AnalysisLlmScenarioOutput> scenarioMap,
        List<AnalysisLlmReferenceNewsItem> referenceNews
) {
}
