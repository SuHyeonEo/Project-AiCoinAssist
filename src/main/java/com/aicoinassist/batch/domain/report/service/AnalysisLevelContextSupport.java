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
                        + ", support strength Δ "
                        + formattingSupport.signed(fact.supportStrengthDelta())
                        + ", support break risk Δ "
                        + formattingSupport.signedRatio(fact.supportBreakRiskDelta())
                        + ", resistance price "
                        + formattingSupport.signedRatio(fact.resistanceRepresentativePriceChangeRate())
                        + ", resistance strength Δ "
                        + formattingSupport.signed(fact.resistanceStrengthDelta())
                        + ", resistance break risk Δ "
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
            facts.add(new AnalysisZoneInteractionFact(
                    AnalysisPriceZoneType.SUPPORT,
                    nearestSupportZone.zoneRank(),
                    nearestSupportZone.interactionType(),
                    "Nearest support zone is %s to %s, currently %s with %d tests and %d breaks."
                            .formatted(
                                    nearestSupportZone.zoneLow().stripTrailingZeros().toPlainString(),
                                    nearestSupportZone.zoneHigh().stripTrailingZeros().toPlainString(),
                                    nearestSupportZone.interactionType().name().toLowerCase().replace('_', ' '),
                                    nearestSupportZone.recentTestCount(),
                                    nearestSupportZone.recentBreakCount()
                            ),
                    nearestSupportZone.triggerFacts()
            ));
        }
        if (nearestResistanceZone != null) {
            facts.add(new AnalysisZoneInteractionFact(
                    AnalysisPriceZoneType.RESISTANCE,
                    nearestResistanceZone.zoneRank(),
                    nearestResistanceZone.interactionType(),
                    "Nearest resistance zone is %s to %s, currently %s with %d tests and %d rejections."
                            .formatted(
                                    nearestResistanceZone.zoneLow().stripTrailingZeros().toPlainString(),
                                    nearestResistanceZone.zoneHigh().stripTrailingZeros().toPlainString(),
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
