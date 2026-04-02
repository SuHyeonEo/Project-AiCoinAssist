package com.aicoinassist.batch.domain.report.repository;

import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class AnalysisReportRepositoryTest {

    @Autowired
    private AnalysisReportRepository analysisReportRepository;

    @Test
    void rejectsDuplicateSymbolTypeBasisVersionAndEngine() {
        Instant analysisBasisTime = Instant.parse("2026-03-09T00:59:59Z");

        analysisReportRepository.saveAndFlush(report(
                analysisBasisTime,
                "snapshotTime=2026-03-09T00:59:59Z;latestCandleOpenTime=2026-03-08T23:59:59Z;priceSourceEventTime=2026-03-09T00:59:30Z",
                "gpt-5.4",
                "{\"summary\":\"first\"}"
        ));

        assertThatThrownBy(() -> analysisReportRepository.saveAndFlush(report(
                analysisBasisTime,
                "snapshotTime=2026-03-09T00:59:59Z;latestCandleOpenTime=2026-03-08T23:59:59Z;priceSourceEventTime=2026-03-09T00:59:30Z",
                "gpt-5.4",
                "{\"summary\":\"second\"}"
        ))).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findsPreviousReportByTypeAndEarlierBasisTime() {
        analysisReportRepository.saveAndFlush(report(
                Instant.parse("2026-02-01T00:00:00Z"),
                "basis-a",
                "gpt-5.4",
                "{\"summary\":\"first\"}"
        ));
        analysisReportRepository.saveAndFlush(report(
                Instant.parse("2026-02-15T00:00:00Z"),
                "basis-b",
                "gpt-5.4",
                "{\"summary\":\"second\"}"
        ));
        analysisReportRepository.saveAndFlush(report(
                Instant.parse("2026-03-01T00:00:00Z"),
                "basis-c",
                "gpt-5.4",
                "{\"summary\":\"third\"}"
        ));

        AnalysisReportEntity previousReport = analysisReportRepository
                .findTopBySymbolAndReportTypeAndAnalysisBasisTimeLessThanOrderByAnalysisBasisTimeDescIdDesc(
                        "BTCUSDT",
                        AnalysisReportType.SHORT_TERM,
                        Instant.parse("2026-03-10T00:00:00Z")
                )
                .orElseThrow();

        assertThat(previousReport.getAnalysisBasisTime()).isEqualTo(Instant.parse("2026-03-01T00:00:00Z"));
        assertThat(previousReport.getSourceDataVersion()).isEqualTo("basis-c");
    }

    private AnalysisReportEntity report(
            Instant analysisBasisTime,
            String sourceDataVersion,
            String analysisEngineVersion,
            String reportPayload
    ) {
        return AnalysisReportEntity.builder()
                                   .symbol("BTCUSDT")
                                   .reportType(AnalysisReportType.SHORT_TERM)
                                   .analysisBasisTime(analysisBasisTime)
                                   .rawReferenceTime(Instant.parse("2026-03-09T00:59:30Z"))
                                   .sourceDataVersion(sourceDataVersion)
                                   .analysisEngineVersion(analysisEngineVersion)
                                   .reportPayload(reportPayload)
                                   .storedTime(Instant.parse("2026-03-09T01:00:10Z"))
                                   .build();
    }
}
