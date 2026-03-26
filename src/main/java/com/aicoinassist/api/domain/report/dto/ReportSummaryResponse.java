package com.aicoinassist.api.domain.report.dto;

public record ReportSummaryResponse(
	ReportMetaResponse meta,
	ReportHeaderResponse header,
	HeroResponse hero,
	ReportSnapshotResponse snapshot,
	MarketParticipationSummaryResponse marketParticipation,
	MarketStructureBoxResponse marketStructureBox,
	ReportSourceMetaResponse sourceMeta
) {
}
