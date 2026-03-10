package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMarketContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceLevel;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceZone;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisRiskFactor;
import com.aicoinassist.batch.domain.report.dto.AnalysisScenario;
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
    private final AnalysisLevelContextSupport levelContextSupport;
    private final AnalysisRiskScenarioFactory riskScenarioFactory;
    private final AnalysisSummarySectionAssembler summarySectionAssembler;
    private final AnalysisMarketContextSectionAssembler marketContextSectionAssembler;

    public AnalysisReportAssembler() {
        AnalysisReportFormattingSupport formattingSupport = new AnalysisReportFormattingSupport();
        this.indicatorStateSupport = new AnalysisIndicatorStateSupport();
        this.comparisonWindowSupport = new AnalysisComparisonWindowSupport(formattingSupport);
        this.derivativeContextSupport = new AnalysisDerivativeContextSupport(formattingSupport);
        this.levelContextSupport = new AnalysisLevelContextSupport(formattingSupport);
        this.riskScenarioFactory = new AnalysisRiskScenarioFactory(
                formattingSupport,
                indicatorStateSupport,
                derivativeContextSupport
        );
        this.summarySectionAssembler = new AnalysisSummarySectionAssembler(
                indicatorStateSupport,
                comparisonWindowSupport,
                derivativeContextSupport,
                formattingSupport
        );
        this.marketContextSectionAssembler = new AnalysisMarketContextSectionAssembler(
                indicatorStateSupport,
                comparisonWindowSupport,
                derivativeContextSupport,
                formattingSupport
        );
    }

    public AnalysisReportPayload assemble(
            MarketIndicatorSnapshotEntity snapshot,
            AnalysisReportType reportType,
            List<AnalysisComparisonFact> comparisonFacts,
            List<AnalysisWindowSummary> windowSummaries,
            AnalysisDerivativeContext derivativeContext,
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
        AnalysisLevelContextPayload effectiveLevelContext = levelContextSupport.prepareLevelContext(
                levelContext,
                supportZones,
                resistanceZones
        );
        AnalysisTrendLabel trendBias = indicatorStateSupport.determineTrendBias(snapshot);
        List<AnalysisRiskFactor> riskFactors = riskScenarioFactory.riskFactors(
                snapshot,
                reportType,
                enrichedDerivativeContext
        );
        List<AnalysisScenario> scenarios = riskScenarioFactory.scenarios(snapshot, trendBias);

        AnalysisSummaryPayload summary = summarySectionAssembler.buildSummary(
                snapshot,
                trendBias,
                reportType,
                comparisonFacts,
                comparisonHighlights,
                windowSummaries,
                enrichedDerivativeContext,
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
