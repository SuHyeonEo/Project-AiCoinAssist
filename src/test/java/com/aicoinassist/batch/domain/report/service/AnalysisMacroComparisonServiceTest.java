package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.macro.entity.MacroContextSnapshotEntity;
import com.aicoinassist.batch.domain.macro.repository.MacroContextSnapshotRepository;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroComparisonFact;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisMacroComparisonServiceTest {

    @Mock
    private MacroContextSnapshotRepository macroContextSnapshotRepository;

    @Test
    void buildFactsReturnsMacroComparisonsForConfiguredReferences() {
        AnalysisMacroComparisonService service = new AnalysisMacroComparisonService(macroContextSnapshotRepository);
        MacroContextSnapshotEntity currentSnapshot = snapshot(
                Instant.parse("2026-03-09T00:00:00Z"),
                "119.84210000",
                "4.12000000",
                "1453.22000000"
        );
        MacroContextSnapshotEntity d30Snapshot = snapshot(
                Instant.parse("2026-02-07T00:00:00Z"),
                "118.07000000",
                "3.96000000",
                "1420.00000000"
        );
        MacroContextSnapshotEntity d90Snapshot = snapshot(
                Instant.parse("2025-12-09T00:00:00Z"),
                "116.00000000",
                "3.70000000",
                "1380.00000000"
        );
        MacroContextSnapshotEntity d180Snapshot = snapshot(
                Instant.parse("2025-09-10T00:00:00Z"),
                "112.00000000",
                "3.50000000",
                "1320.00000000"
        );

        when(macroContextSnapshotRepository.findTopBySnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                Instant.parse("2026-02-07T00:00:00Z")
        )).thenReturn(Optional.of(d30Snapshot));
        when(macroContextSnapshotRepository.findTopBySnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                Instant.parse("2025-12-09T00:00:00Z")
        )).thenReturn(Optional.of(d90Snapshot));
        when(macroContextSnapshotRepository.findTopBySnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                Instant.parse("2025-09-10T00:00:00Z")
        )).thenReturn(Optional.of(d180Snapshot));

        List<AnalysisMacroComparisonFact> facts = service.buildFacts(currentSnapshot, AnalysisReportType.LONG_TERM);

        assertThat(facts).extracting(AnalysisMacroComparisonFact::reference)
                         .containsExactly(
                                 AnalysisComparisonReference.D30,
                                 AnalysisComparisonReference.D90,
                                 AnalysisComparisonReference.D180
                         );
        assertThat(facts.get(0).dxyProxyChangeRate()).isEqualByComparingTo("0.01500889");
        assertThat(facts.get(0).us10yYieldValueChange()).isEqualByComparingTo("0.16000000");
        assertThat(facts.get(0).usdKrwChangeRate()).isEqualByComparingTo("0.02339437");
    }

    private MacroContextSnapshotEntity snapshot(
            Instant snapshotTime,
            String dxyValue,
            String us10yValue,
            String usdKrwValue
    ) {
        return MacroContextSnapshotEntity.builder()
                                         .snapshotTime(snapshotTime)
                                         .dxyObservationDate(LocalDate.ofInstant(snapshotTime, java.time.ZoneOffset.UTC))
                                         .us10yYieldObservationDate(LocalDate.ofInstant(snapshotTime, java.time.ZoneOffset.UTC))
                                         .usdKrwObservationDate(LocalDate.ofInstant(snapshotTime, java.time.ZoneOffset.UTC))
                                         .sourceDataVersion("macro-basis")
                                         .dxyProxyValue(new BigDecimal(dxyValue))
                                         .us10yYieldValue(new BigDecimal(us10yValue))
                                         .usdKrwValue(new BigDecimal(usdKrwValue))
                                         .build();
    }
}
