package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisPriceZone;
import com.aicoinassist.batch.domain.report.dto.AnalysisZoneInteractionFact;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneType;

import java.util.ArrayList;
import java.util.List;

class AnalysisLevelContextSupport {

    private final AnalysisReportFormattingSupport formattingSupport;

    AnalysisLevelContextSupport(AnalysisReportFormattingSupport formattingSupport) {
        this.formattingSupport = formattingSupport;
    }

    AnalysisLevelContextPayload prepareLevelContext(
            AnalysisLevelContextPayload levelContext,
            List<AnalysisPriceZone> supportZones,
            List<AnalysisPriceZone> resistanceZones
    ) {
        return levelContext == null
                ? fallbackLevelContext(supportZones, resistanceZones)
                : enrichLevelContext(levelContext);
    }

    private AnalysisLevelContextPayload enrichLevelContext(AnalysisLevelContextPayload levelContext) {
        return new AnalysisLevelContextPayload(
                levelContext.nearestSupportZone(),
                levelContext.nearestResistanceZone(),
                levelContext.zoneInteractionFacts(),
                levelContext.supportBreakRisk(),
                levelContext.resistanceBreakRisk(),
                levelContext.comparisonFacts(),
                levelContextHighlights(levelContext.comparisonFacts())
        );
    }

    private AnalysisLevelContextPayload fallbackLevelContext(
            List<AnalysisPriceZone> supportZones,
            List<AnalysisPriceZone> resistanceZones
    ) {
        AnalysisPriceZone nearestSupportZone = supportZones.isEmpty() ? null : supportZones.get(0);
        AnalysisPriceZone nearestResistanceZone = resistanceZones.isEmpty() ? null : resistanceZones.get(0);
        return new AnalysisLevelContextPayload(
                nearestSupportZone,
                nearestResistanceZone,
                zoneInteractionFacts(nearestSupportZone, nearestResistanceZone),
                null,
                null,
                List.of(),
                List.of()
        );
    }

    private List<AnalysisLevelContextHighlight> levelContextHighlights(
            List<AnalysisLevelContextComparisonFact> comparisonFacts
    ) {
        if (comparisonFacts == null || comparisonFacts.isEmpty()) {
            return List.of();
        }
        return comparisonFacts.stream()
                              .limit(2)
                              .map(this::toLevelContextHighlight)
                              .toList();
    }

    private AnalysisLevelContextHighlight toLevelContextHighlight(AnalysisLevelContextComparisonFact fact) {
        return new AnalysisLevelContextHighlight(
                fact.reference(),
                fact.reference().name() + " level context",
                fact.reference().name()
                        + " keeps support price "
                        + formattingSupport.signedRatio(fact.supportRepresentativePriceChangeRate())
                        + ", support strength delta "
                        + formattingSupport.signed(fact.supportStrengthDelta())
                        + ", support break risk delta "
                        + formattingSupport.signedRatio(fact.supportBreakRiskDelta())
                        + ", resistance price "
                        + formattingSupport.signedRatio(fact.resistanceRepresentativePriceChangeRate())
                        + ", resistance strength delta "
                        + formattingSupport.signed(fact.resistanceStrengthDelta())
                        + ", resistance break risk delta "
                        + formattingSupport.signedRatio(fact.resistanceBreakRiskDelta())
                        + ", support interaction "
                        + formattingSupport.interactionShift(fact.currentSupportInteractionType(), fact.referenceSupportInteractionType())
                        + ", resistance interaction "
                        + formattingSupport.interactionShift(fact.currentResistanceInteractionType(), fact.referenceResistanceInteractionType())
                        + "."
        );
    }

    private List<AnalysisZoneInteractionFact> zoneInteractionFacts(
            AnalysisPriceZone nearestSupportZone,
            AnalysisPriceZone nearestResistanceZone
    ) {
        List<AnalysisZoneInteractionFact> facts = new ArrayList<>();
        if (nearestSupportZone != null) {
            String supportLabel = formattingSupport.zoneLabel(nearestSupportZone.zoneLow(), nearestSupportZone.zoneHigh());
            facts.add(new AnalysisZoneInteractionFact(
                    AnalysisPriceZoneType.SUPPORT,
                    nearestSupportZone.zoneRank(),
                    nearestSupportZone.interactionType(),
                    "Nearest support %s is currently %s with %d tests and %d breaks."
                            .formatted(
                                    supportLabel,
                                    nearestSupportZone.interactionType().name().toLowerCase().replace('_', ' '),
                                    nearestSupportZone.recentTestCount(),
                                    nearestSupportZone.recentBreakCount()
                            ),
                    nearestSupportZone.triggerFacts()
            ));
        }
        if (nearestResistanceZone != null) {
            String resistanceLabel = formattingSupport.zoneLabel(nearestResistanceZone.zoneLow(), nearestResistanceZone.zoneHigh());
            facts.add(new AnalysisZoneInteractionFact(
                    AnalysisPriceZoneType.RESISTANCE,
                    nearestResistanceZone.zoneRank(),
                    nearestResistanceZone.interactionType(),
                    "Nearest resistance %s is currently %s with %d tests and %d rejections."
                            .formatted(
                                    resistanceLabel,
                                    nearestResistanceZone.interactionType().name().toLowerCase().replace('_', ' '),
                                    nearestResistanceZone.recentTestCount(),
                                    nearestResistanceZone.recentRejectionCount()
                            ),
                    nearestResistanceZone.triggerFacts()
            ));
        }
        return facts;
    }
}
