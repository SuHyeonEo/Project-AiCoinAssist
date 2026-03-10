package com.aicoinassist.batch.domain.report.dto;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneInteractionType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisPriceZoneType;

import java.util.List;

public record AnalysisZoneInteractionFact(
        AnalysisPriceZoneType zoneType,
        Integer zoneRank,
        AnalysisPriceZoneInteractionType interactionType,
        String summary,
        List<String> triggerFacts
) {
}
