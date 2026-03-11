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
                280,
                3,
                120,
                7,
                180,
                4,
                160,
                220,
                2,
                110,
                2,
                110,
                220,
                2,
                180,
                2,
                110,
                180,
                5,
                160,
                180
        );
    }
}
