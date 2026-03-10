package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeDirection;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeSeverity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisExternalContextSnapshotServiceTest extends AnalysisReportServiceFixtures {

    private final AnalysisExternalContextSnapshotService service = new AnalysisExternalContextSnapshotService();

    @Test
    void createBuildsCompositeSnapshotFromExternalContexts() {
        var snapshot = service.create(
                "BTCUSDT",
                AnalysisReportType.SHORT_TERM,
                derivativeContext(),
                macroContext(),
                sentimentContext(),
                onchainContext()
        );

        assertThat(snapshot.symbol()).isEqualTo("BTCUSDT");
        assertThat(snapshot.snapshotTime()).isNotNull();
        assertThat(snapshot.sourceDataVersion()).contains("derivative=", "macro=", "sentiment=", "onchain=");
        assertThat(snapshot.regimeSignals()).isNotEmpty();
        assertThat(snapshot.primarySignalTitle()).isNotBlank();
        assertThat(snapshot.highestSeverity()).isEqualTo(AnalysisExternalRegimeSeverity.HIGH);
        assertThat(snapshot.dominantDirection()).isIn(
                AnalysisExternalRegimeDirection.HEADWIND,
                AnalysisExternalRegimeDirection.CAUTIONARY,
                AnalysisExternalRegimeDirection.SUPPORTIVE
        );
        assertThat(snapshot.supportiveSignalCount()
                           + snapshot.cautionarySignalCount()
                           + snapshot.headwindSignalCount())
                .isEqualTo(snapshot.regimeSignals().size());
    }
}
