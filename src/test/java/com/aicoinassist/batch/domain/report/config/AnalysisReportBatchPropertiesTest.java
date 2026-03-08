package com.aicoinassist.batch.domain.report.config;

import com.aicoinassist.batch.domain.market.enumtype.AssetType;
import com.aicoinassist.batch.domain.market.enumtype.CandleInterval;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisReportBatchPropertiesTest {

    @Test
    void snapshotIntervalsDeduplicatesAndKeepsReportTypeOrder() {
        AnalysisReportBatchProperties properties = new AnalysisReportBatchProperties(
                "report-assembler-v1",
                List.of(AssetType.BTC, AssetType.ETH),
                List.of(
                        AnalysisReportType.SHORT_TERM,
                        AnalysisReportType.LONG_TERM,
                        AnalysisReportType.MID_TERM,
                        AnalysisReportType.SHORT_TERM
                ),
                300000L
        );

        assertThat(properties.snapshotIntervals()).containsExactly(
                CandleInterval.ONE_HOUR,
                CandleInterval.ONE_DAY,
                CandleInterval.FOUR_HOUR
        );
    }
}
