package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AnalysisLlmNarrativeInputPayload(
        String symbol,
        AnalysisReportType reportType,
        Instant analysisBasisTime,
        Instant rawReferenceTime,
        String sourceDataVersion,
        String analysisEngineVersion,
        AnalysisLlmSharedContextReference sharedContextReference,
        AnalysisLlmExecutiveSummary executiveSummary,
        List<AnalysisContextHeadlinePayload> signalHeadlines,
        List<String> primaryFacts,
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
        List<AnalysisRiskFactor> riskFactors,
        List<AnalysisScenario> scenarios,
        List<AnalysisContinuityNote> continuityNotes
) {
}
