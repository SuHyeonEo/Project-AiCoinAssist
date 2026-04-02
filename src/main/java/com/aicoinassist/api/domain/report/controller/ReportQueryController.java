package com.aicoinassist.api.domain.report.controller;

import com.aicoinassist.api.domain.report.dto.ReportDetailResponse;
import com.aicoinassist.api.domain.report.dto.ReportHistoryResponse;
import com.aicoinassist.api.domain.report.dto.ReportSummaryResponse;
import com.aicoinassist.api.domain.report.enumtype.ReportType;
import com.aicoinassist.api.domain.report.service.ReportReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Report retrieval APIs for latest detail, detail, and history")
public class ReportQueryController {

	private final ReportReadService reportReadService;

	@GetMapping("/latest/detail")
	@Operation(summary = "Get latest report detail", description = "Returns the latest report detail view for the given symbol and report type.")
	public ReportDetailResponse getLatestDetail(
		@RequestParam @Pattern(regexp = "^[A-Z0-9]{2,20}USDT$") String symbol,
		@RequestParam ReportType reportType
	) {
		return reportReadService.getLatestDetail(symbol, reportType);
	}

	@GetMapping("/latest/summary")
	@Operation(summary = "Get latest report summary", description = "Returns the latest report summary view for the given symbol and report type.")
	public ReportSummaryResponse getLatestSummary(
		@RequestParam @Pattern(regexp = "^[A-Z0-9]{2,20}USDT$") String symbol,
		@RequestParam ReportType reportType
	) {
		return reportReadService.getLatestSummary(symbol, reportType);
	}

	@GetMapping("/{reportId}")
	@Operation(summary = "Get report detail by id", description = "Returns the report detail view for a persisted report id.")
	public ReportDetailResponse getDetail(@PathVariable Long reportId) {
		return reportReadService.getDetail(reportId);
	}

	@GetMapping("/history")
	@Operation(summary = "Get report history", description = "Returns recent report history items for the given symbol and report type.")
	public ReportHistoryResponse getHistory(
		@RequestParam @Pattern(regexp = "^[A-Z0-9]{2,20}USDT$") String symbol,
		@RequestParam ReportType reportType,
		@RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
	) {
		return reportReadService.getHistory(symbol, reportType, limit);
	}
}
