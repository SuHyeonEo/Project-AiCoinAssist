package com.aicoinassist.api.domain.report.dto;

import com.aicoinassist.api.domain.report.enumtype.ReportType;
import java.util.List;

public record ReportHistoryResponse(
	String symbol,
	ReportType reportType,
	int limit,
	List<ReportHistoryItemResponse> items
) {
}
