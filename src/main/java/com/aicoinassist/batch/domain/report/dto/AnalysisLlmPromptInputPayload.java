package com.aicoinassist.batch.domain.report.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnalysisLlmPromptInputPayload(
        AnalysisLlmPromptMetadata metadata,
        AnalysisLlmSharedContextReference sharedContextReference,
        AnalysisLlmExecutiveSummary executiveSummary,
        List<AnalysisContextHeadlinePayload> signalHeadlines,
        List<String> primaryFacts,
        List<String> marketParticipationFacts,
        List<String> marketStructureFacts,
        List<String> derivativeStructureFacts,
        List<String> macroStructureFacts,
        List<String> sentimentStructureFacts,
        List<String> onchainStructureFacts,
        List<String> externalStructureFacts,
        AnalysisLlmServerMarketStructureInput serverMarketStructure,
        List<String> levelStructureFacts,
        List<String> marketStructureBoxFacts,
        List<AnalysisLlmDomainFactBlock> domainFactBlocks,
        List<AnalysisGptCrossSignal> crossSignals,
        List<AnalysisLlmScenarioGuidance> scenarioGuidance,
        AnalysisLlmRiskScenarioInput limitedRisksScenarios
) {
}
