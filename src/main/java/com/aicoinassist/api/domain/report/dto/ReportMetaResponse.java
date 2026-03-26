package com.aicoinassist.api.domain.report.dto;

import com.aicoinassist.api.domain.report.enumtype.ReportType;
import java.time.Instant;

public record ReportMetaResponse(
	Long reportId,
	Long narrativeId,
	String symbol,
	ReportType reportType,
	Instant analysisBasisTime,
	Instant rawReferenceTime,
	Instant priceSourceEventTime,
	Instant reportStoredTime,
	boolean narrativeAvailable
) {
}
