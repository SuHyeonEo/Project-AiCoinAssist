package com.aicoinassist.batch.domain.report.config;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashSet;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "batch.analysis-report")
public record AnalysisReportBatchProperties(
        @NotBlank String engineVersion,
        @NotEmpty List<AssetType> assetTypes,
        @NotEmpty List<AnalysisReportType> reportTypes,
        @Min(1000) long fixedDelayMs
) {

    public List<CandleInterval> snapshotIntervals() {
        LinkedHashSet<CandleInterval> intervals = new LinkedHashSet<>();
        for (AnalysisReportType reportType : reportTypes) {
            intervals.add(intervalFor(reportType));
        }
        return List.copyOf(intervals);
    }

    private CandleInterval intervalFor(AnalysisReportType reportType) {
        return switch (reportType) {
            case SHORT_TERM -> CandleInterval.ONE_HOUR;
            case MID_TERM -> CandleInterval.FOUR_HOUR;
            case LONG_TERM -> CandleInterval.ONE_DAY;
        };
    }
}
