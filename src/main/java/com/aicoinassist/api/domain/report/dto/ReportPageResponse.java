package com.aicoinassist.api.domain.report.dto;

import java.util.List;

public record ReportPageResponse(
	HeroResponse hero,
	ExecutiveConclusionResponse executiveConclusion,
	MarketParticipationSummaryResponse marketParticipation,
	List<DomainAnalysisResponse> domainCards,
	MarketStructureBoxResponse marketStructureBox,
	CrossSignalIntegrationResponse crossSignal,
	List<ScenarioViewResponse> scenarios,
	LevelSummaryResponse levels,
	ExternalContextSummaryResponse externalContext,
	SharedContextSummaryResponse sharedContext,
	List<ReferenceNewsItemResponse> referenceNews,
	ReportSourceMetaResponse sourceMeta
) {
}
