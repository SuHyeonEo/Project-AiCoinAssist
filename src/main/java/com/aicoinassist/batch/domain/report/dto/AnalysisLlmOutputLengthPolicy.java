package com.aicoinassist.batch.domain.report.dto;

public record AnalysisLlmOutputLengthPolicy(
        int executiveConclusionSummaryMaxChars,
        int executiveConclusionFactorMaxItems,
        int executiveConclusionFactorItemMaxChars,
        int domainAnalysisMaxItems,
        int domainCurrentSignalMaxChars,
        int domainKeyFactsMaxItems,
        int domainKeyFactMaxChars,
        int domainInterpretationMaxChars,
        int domainCaveatsMaxItems,
        int domainCaveatMaxChars,
        int crossSignalListMaxItems,
        int crossSignalItemMaxChars,
        int crossSignalCombinedStructureMaxChars,
        int scenarioMaxItems,
        int scenarioConditionMaxChars,
        int scenarioListMaxItems,
        int scenarioItemMaxChars,
        int scenarioInterpretationMaxChars,
        int referenceNewsMaxItems,
        int referenceNewsTitleMaxChars,
        int referenceNewsWhyItMattersMaxChars
) {

    public static AnalysisLlmOutputLengthPolicy defaultPolicy() {
        return new AnalysisLlmOutputLengthPolicy(
                220,
                3,
                100,
                7,
                140,
                3,
                120,
                180,
                2,
                90,
                2,
                90,
                180,
                2,
                140,
                2,
                90,
                150,
                3,
                140,
                140
        );
    }
}
