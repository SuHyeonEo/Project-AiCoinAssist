package com.aicoinassist.api.domain.report.dto;

public record ReportDetailResponse(
	ReportMetaResponse meta,
	ReportHeaderResponse header,
	ReportPageResponse page
) {
}
