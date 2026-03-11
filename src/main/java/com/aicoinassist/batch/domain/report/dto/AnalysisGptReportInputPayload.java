package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;

import java.time.Instant;
import java.util.List;

public record AnalysisGptReportInputPayload(
        String symbol,
        AnalysisReportType reportType,
        Instant analysisBasisTime,
        Instant rawReferenceTime,
        String sourceDataVersion,
        String analysisEngineVersion,
        AnalysisSummaryPayload summary,
        AnalysisCurrentStatePayload currentState,
        AnalysisComparisonContextPayload comparisonContext,
        AnalysisWindowContextPayload windowContext,
        AnalysisDerivativeContextSummaryPayload derivativeContext,
        AnalysisDerivativeContext derivativeFactContext,
        AnalysisMacroContextSummaryPayload macroContext,
        AnalysisMacroContext macroFactContext,
        AnalysisSentimentContextSummaryPayload sentimentContext,
        AnalysisSentimentContext sentimentFactContext,
        AnalysisOnchainContextSummaryPayload onchainContext,
        AnalysisOnchainContext onchainFactContext,
        AnalysisLevelContextPayload levelContext,
        AnalysisExternalContextCompositePayload externalContextComposite,
        List<AnalysisWindowSummary> windowSummaries,
        List<AnalysisContextHeadlinePayload> signalHeadlines,
        List<String> primaryFacts,
        List<AnalysisGptCrossSignal> crossSignals,
        List<AnalysisRiskFactor> riskFactors,
        List<AnalysisScenario> scenarios,
        List<AnalysisContinuityNote> continuityNotes
) {
}
