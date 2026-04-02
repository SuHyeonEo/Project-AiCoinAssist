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
                        + " 기준 지지 가격은 "
                        + formattingSupport.signedRatio(fact.supportRepresentativePriceChangeRate())
                        + ", 지지 강도 변화는 "
                        + formattingSupport.signed(fact.supportStrengthDelta())
                        + ", 지지 이탈 위험 변화는 "
                        + formattingSupport.signedRatio(fact.supportBreakRiskDelta())
                        + ", 저항 가격은 "
                        + formattingSupport.signedRatio(fact.resistanceRepresentativePriceChangeRate())
                        + ", 저항 강도 변화는 "
                        + formattingSupport.signed(fact.resistanceStrengthDelta())
                        + ", 저항 돌파 위험 변화는 "
                        + formattingSupport.signedRatio(fact.resistanceBreakRiskDelta())
                        + ", 지지 상호작용은 "
                        + formattingSupport.interactionShift(fact.currentSupportInteractionType(), fact.referenceSupportInteractionType())
                        + ", 저항 상호작용은 "
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
                    "가까운 지지 구간 %s은 현재 %s 상태이며, 최근 테스트 %d회와 이탈 %d회가 확인됐습니다."
                            .formatted(
                                    supportLabel,
                                    formattingSupport.interactionLabel(nearestSupportZone.interactionType()),
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
                    "가까운 저항 구간 %s은 현재 %s 상태이며, 최근 테스트 %d회와 저항 확인 %d회가 집계됐습니다."
                            .formatted(
                                    resistanceLabel,
                                    formattingSupport.interactionLabel(nearestResistanceZone.interactionType()),
                                    nearestResistanceZone.recentTestCount(),
                                    nearestResistanceZone.recentRejectionCount()
                            ),
                    nearestResistanceZone.triggerFacts()
            ));
        }
        return facts;
    }
}
