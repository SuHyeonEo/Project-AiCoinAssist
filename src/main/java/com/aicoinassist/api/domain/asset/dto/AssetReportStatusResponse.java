package com.aicoinassist.api.domain.asset.dto;

import com.aicoinassist.api.domain.report.enumtype.ReportType;
import java.time.Instant;

public record AssetReportStatusResponse(
	ReportType reportType,
	boolean available,
	Long reportId,
	Instant analysisBasisTime,
	Instant reportStoredTime,
	String headline,
	String outlook,
	String overallTone,
	boolean narrativeAvailable
) {
}
