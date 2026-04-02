package com.aicoinassist.batch.domain.report.dto;

public record AnalysisLlmOutputLengthPolicy(
        int heroSummaryMaxChars,
        int executiveConclusionSummaryMaxChars,
        int executiveConclusionFactorMaxItems,
        int executiveConclusionFactorItemMaxChars,
        int executiveConclusionTacticalViewMaxChars,
        int domainAnalysisMaxItems,
        int domainStatusMaxChars,
        int domainInterpretationMaxChars,
        int domainWatchPointMaxChars,
        int marketStructureValueMaxChars,
        int marketStructureLabelMaxChars,
        int marketStructureBasisMaxChars,
        int marketStructureInterpretationMaxChars,
        int crossSignalSummaryMaxChars,
        int crossSignalListMaxItems,
        int crossSignalItemMaxChars,
        int crossSignalPositioningTakeMaxChars,
        int scenarioMaxItems,
        int scenarioTitleMaxChars,
        int scenarioConditionMaxChars,
        int scenarioFieldMaxChars,
        int scenarioInterpretationMaxChars
) {

    public static AnalysisLlmOutputLengthPolicy defaultPolicy() {
        return new AnalysisLlmOutputLengthPolicy(
                80,
                240,
                3,
                95,
                140,
                7,
                32,
                190,
                70,
                32,
                48,
                80,
                260,
                140,
                4,
                36,
                120,
                3,
                40,
                90,
                110,
                160
        );
    }
}
