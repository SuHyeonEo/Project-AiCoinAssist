package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMarketContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceLevel;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceZone;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisRiskFactor;
import com.aicoinassist.batch.domain.report.dto.AnalysisScenario;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisTrendLabel;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AnalysisReportAssembler {

    private final AnalysisIndicatorStateSupport indicatorStateSupport;
    private final AnalysisComparisonWindowSupport comparisonWindowSupport;
    private final AnalysisDerivativeContextSupport derivativeContextSupport;
    private final AnalysisMacroContextSupport macroContextSupport;
    private final AnalysisSentimentContextSupport sentimentContextSupport;
    private final AnalysisOnchainContextSupport onchainContextSupport;
    private final AnalysisLevelContextSupport levelContextSupport;
    private final AnalysisRiskScenarioFactory riskScenarioFactory;
    private final AnalysisSummarySectionAssembler summarySectionAssembler;
    private final AnalysisMarketContextSectionAssembler marketContextSectionAssembler;

    public AnalysisReportAssembler() {
        AnalysisReportFormattingSupport formattingSupport = new AnalysisReportFormattingSupport();
        this.indicatorStateSupport = new AnalysisIndicatorStateSupport();
        this.comparisonWindowSupport = new AnalysisComparisonWindowSupport(formattingSupport);
        this.derivativeContextSupport = new AnalysisDerivativeContextSupport(formattingSupport);
        this.macroContextSupport = new AnalysisMacroContextSupport(formattingSupport);
        this.sentimentContextSupport = new AnalysisSentimentContextSupport(formattingSupport);
        this.onchainContextSupport = new AnalysisOnchainContextSupport(formattingSupport);
        this.levelContextSupport = new AnalysisLevelContextSupport(formattingSupport);
        this.riskScenarioFactory = new AnalysisRiskScenarioFactory(
                formattingSupport,
                indicatorStateSupport,
                derivativeContextSupport,
                macroContextSupport,
                sentimentContextSupport,
                onchainContextSupport
        );
        this.summarySectionAssembler = new AnalysisSummarySectionAssembler(
                indicatorStateSupport,
                comparisonWindowSupport,
                derivativeContextSupport,
                macroContextSupport,
                sentimentContextSupport,
                onchainContextSupport,
                formattingSupport
        );
        this.marketContextSectionAssembler = new AnalysisMarketContextSectionAssembler(
                indicatorStateSupport,
                comparisonWindowSupport,
                derivativeContextSupport,
                macroContextSupport,
                sentimentContextSupport,
                onchainContextSupport,
                formattingSupport
        );
    }

    public AnalysisReportPayload assemble(
            MarketIndicatorSnapshotEntity snapshot,
            AnalysisReportType reportType,
            List<AnalysisComparisonFact> comparisonFacts,
            List<AnalysisWindowSummary> windowSummaries,
            AnalysisDerivativeContext derivativeContext,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            List<AnalysisContinuityNote> continuityNotes,
            AnalysisLevelContextPayload levelContext,
            List<AnalysisPriceLevel> supportLevels,
            List<AnalysisPriceLevel> resistanceLevels,
            List<AnalysisPriceZone> supportZones,
            List<AnalysisPriceZone> resistanceZones
    ) {
        List<AnalysisComparisonHighlight> comparisonHighlights = comparisonWindowSupport.comparisonHighlights(
                reportType,
                comparisonFacts
        );
        List<AnalysisWindowHighlight> windowHighlights = comparisonWindowSupport.windowHighlights(
                reportType,
                windowSummaries
        );
        AnalysisDerivativeContext enrichedDerivativeContext = derivativeContextSupport.enrichDerivativeContext(
                reportType,
                derivativeContext
        );
        AnalysisMacroContext enrichedMacroContext = macroContextSupport.enrichMacroContext(
                reportType,
                macroContext
        );
        AnalysisSentimentContext enrichedSentimentContext = sentimentContextSupport.enrichSentimentContext(
                reportType,
                sentimentContext
        );
        AnalysisOnchainContext enrichedOnchainContext = onchainContextSupport.enrichOnchainContext(
                reportType,
                onchainContext
        );
        AnalysisLevelContextPayload effectiveLevelContext = levelContextSupport.prepareLevelContext(
                levelContext,
                supportZones,
                resistanceZones
        );
        AnalysisTrendLabel trendBias = indicatorStateSupport.determineTrendBias(snapshot);
        List<AnalysisRiskFactor> riskFactors = riskScenarioFactory.riskFactors(
                snapshot,
                reportType,
                enrichedDerivativeContext,
                enrichedMacroContext,
                enrichedSentimentContext,
                enrichedOnchainContext
        );
        List<AnalysisScenario> scenarios = riskScenarioFactory.scenarios(
                snapshot,
                trendBias,
                reportType,
                enrichedDerivativeContext,
                enrichedMacroContext,
                enrichedSentimentContext,
                enrichedOnchainContext
        );

        AnalysisSummaryPayload summary = summarySectionAssembler.buildSummary(
                snapshot,
                trendBias,
                reportType,
                comparisonFacts,
                comparisonHighlights,
                windowSummaries,
                enrichedDerivativeContext,
                enrichedMacroContext,
                enrichedSentimentContext,
                enrichedOnchainContext,
                continuityNotes,
                effectiveLevelContext
        );
        AnalysisMarketContextPayload marketContext = marketContextSectionAssembler.buildMarketContext(
                snapshot,
                trendBias,
                reportType,
                comparisonFacts,
                comparisonHighlights,
                windowHighlights,
                windowSummaries,
                enrichedDerivativeContext,
                enrichedMacroContext,
                enrichedSentimentContext,
                enrichedOnchainContext,
                continuityNotes,
                effectiveLevelContext,
                riskFactors
        );

        return new AnalysisReportPayload(
                summary,
                marketContext,
                comparisonFacts,
                comparisonHighlights,
                windowHighlights,
                continuityNotes,
                windowSummaries,
                enrichedDerivativeContext,
                enrichedMacroContext,
                enrichedSentimentContext,
                enrichedOnchainContext,
                supportLevels,
                resistanceLevels,
                supportZones,
                resistanceZones,
                effectiveLevelContext.nearestSupportZone(),
                effectiveLevelContext.nearestResistanceZone(),
                effectiveLevelContext.zoneInteractionFacts(),
                riskFactors,
                scenarios
        );
    }
}
