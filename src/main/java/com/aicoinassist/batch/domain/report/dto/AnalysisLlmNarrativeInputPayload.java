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
        AnalysisLlmExecutiveSummary executiveSummary,
        List<AnalysisContextHeadlinePayload> signalHeadlines,
        List<String> primaryFacts,
        List<AnalysisLlmDomainFactBlock> domainFactBlocks,
        List<AnalysisGptCrossSignal> crossSignals,
        List<AnalysisRiskFactor> riskFactors,
        List<AnalysisScenario> scenarios,
        List<AnalysisContinuityNote> continuityNotes
) {
}
