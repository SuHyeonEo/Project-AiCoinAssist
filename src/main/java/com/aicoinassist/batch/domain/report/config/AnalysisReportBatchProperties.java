package com.aicoinassist.batch.domain.report.config;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
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
        boolean shortTermEnabled,
        @NotBlank String shortTermCron,
        boolean midTermEnabled,
        @NotBlank String midTermCron,
        boolean longTermEnabled,
        @NotBlank String longTermCron,
        @NotBlank String zone
) {

    public List<CandleInterval> snapshotIntervals(List<AnalysisReportType> targetReportTypes) {
        LinkedHashSet<CandleInterval> intervals = new LinkedHashSet<>();
        for (AnalysisReportType reportType : targetReportTypes) {
            intervals.add(intervalFor(reportType));
        }
        return List.copyOf(intervals);
    }

    public boolean isEnabled(AnalysisReportType reportType) {
        return reportTypes.contains(reportType) && scheduleFor(reportType).enabled();
    }

    public ScheduleProperties scheduleFor(AnalysisReportType reportType) {
        return switch (reportType) {
            case SHORT_TERM -> new ScheduleProperties(shortTermEnabled, shortTermCron, zone);
            case MID_TERM -> new ScheduleProperties(midTermEnabled, midTermCron, zone);
            case LONG_TERM -> new ScheduleProperties(longTermEnabled, longTermCron, zone);
        };
    }

    private CandleInterval intervalFor(AnalysisReportType reportType) {
        return switch (reportType) {
            case SHORT_TERM -> CandleInterval.ONE_HOUR;
            case MID_TERM -> CandleInterval.FOUR_HOUR;
            case LONG_TERM -> CandleInterval.ONE_DAY;
        };
    }

    public record ScheduleProperties(
            boolean enabled,
            @NotBlank String cron,
            @NotBlank String zone
    ) {
    }
}
