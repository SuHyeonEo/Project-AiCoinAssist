package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketExternalContextSnapshotEntity;
import com.aicoinassist.batch.domain.market.repository.MarketExternalContextSnapshotRepository;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimePersistence;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimeStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimeTransition;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeDirection;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeTransitionType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisExternalContextComparisonServiceTest {

    @Mock
    private MarketExternalContextSnapshotRepository marketExternalContextSnapshotRepository;

    @Test
    void buildFactsAndHighlightsReturnsExternalCompositeComparisons() {
        AnalysisExternalContextComparisonService service = new AnalysisExternalContextComparisonService(
                marketExternalContextSnapshotRepository
        );
        MarketExternalContextSnapshotEntity currentSnapshot = snapshot(
                Instant.parse("2026-03-09T00:59:30Z"),
                "1.33333333",
                "HEADWIND",
                "HIGH",
                0,
                2,
                2,
                "Dollar strength regime"
        );
        MarketExternalContextSnapshotEntity d30Snapshot = snapshot(
                Instant.parse("2026-02-07T00:59:30Z"),
                "0.25000000",
                "SUPPORTIVE",
                "HIGH",
                1,
                1,
                1,
                "Funding crowding regime"
        );

        when(marketExternalContextSnapshotRepository.findTopBySymbolAndSnapshotTimeLessThanEqualOrderBySnapshotTimeDescIdDesc(
                "BTCUSDT",
                Instant.parse("2026-02-07T00:59:30Z")
        )).thenReturn(Optional.of(d30Snapshot));

        List<AnalysisExternalContextComparisonFact> facts = service.buildFacts(currentSnapshot, AnalysisReportType.LONG_TERM);
        List<AnalysisExternalContextHighlight> highlights = service.buildHighlights(currentSnapshot, facts);
        List<AnalysisExternalRegimeTransition> transitions = service.buildTransitions(currentSnapshot, facts);
        AnalysisExternalRegimePersistence persistence = service.buildPersistence(
                currentSnapshot,
                List.of(new AnalysisExternalContextWindowSummary(
                        com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_30D,
                        Instant.parse("2026-02-07T00:59:30Z"),
                        Instant.parse("2026-03-09T00:59:30Z"),
                        30,
                        new BigDecimal("0.95000000"),
                        new BigDecimal("0.40350877"),
                        4,
                        10,
                        16,
                        8
                ))
        );
        AnalysisExternalRegimeStatePayload state = service.buildState(
                currentSnapshot,
                transitions,
                persistence,
                List.of(new AnalysisExternalContextWindowSummary(
                        com.aicoinassist.batch.domain.market.enumtype.MarketWindowType.LAST_30D,
                        Instant.parse("2026-02-07T00:59:30Z"),
                        Instant.parse("2026-03-09T00:59:30Z"),
                        30,
                        new BigDecimal("0.95000000"),
                        new BigDecimal("0.40350877"),
                        4,
                        10,
                        16,
                        8
                ))
        );

        assertThat(facts).extracting(AnalysisExternalContextComparisonFact::reference)
                         .containsExactly(AnalysisComparisonReference.D30);
        assertThat(facts.get(0).compositeRiskScoreDelta()).isEqualByComparingTo("1.08333333");
        assertThat(facts.get(0).dominantDirectionChanged()).isTrue();
        assertThat(facts.get(0).primarySignalChanged()).isTrue();

        assertThat(highlights).hasSize(1);
        assertThat(highlights.get(0).title()).isEqualTo("External regime direction changed");
        assertThat(highlights.get(0).importance()).isEqualTo(AnalysisContextHeadlineImportance.HIGH);
        assertThat(highlights.get(0).reference()).isEqualTo(AnalysisComparisonReference.D30);
        assertThat(transitions).hasSize(1);
        assertThat(transitions.get(0).transitionType()).isEqualTo(AnalysisExternalRegimeTransitionType.TRANSITION_TO_HEADWIND);
        assertThat(transitions.get(0).resultingDirection()).isEqualTo(AnalysisExternalRegimeDirection.HEADWIND);
        assertThat(persistence).isNotNull();
        assertThat(persistence.dominantDirection()).isEqualTo(AnalysisExternalRegimeDirection.HEADWIND);
        assertThat(persistence.persistenceScore()).isEqualByComparingTo("0.45333333");
        assertThat(state).isNotNull();
        assertThat(state.dominantDirection()).isEqualTo(AnalysisExternalRegimeDirection.HEADWIND);
        assertThat(state.reversalRiskScore()).isGreaterThan(new BigDecimal("0.50"));
    }

    private MarketExternalContextSnapshotEntity snapshot(
            Instant snapshotTime,
            String compositeRiskScore,
            String dominantDirection,
            String highestSeverity,
            int supportiveSignalCount,
            int cautionarySignalCount,
            int headwindSignalCount,
            String primarySignalTitle
    ) {
        return MarketExternalContextSnapshotEntity.builder()
                                                  .symbol("BTCUSDT")
                                                  .snapshotTime(snapshotTime)
                                                  .sourceDataVersion("external=" + snapshotTime)
                                                  .compositeRiskScore(new BigDecimal(compositeRiskScore))
                                                  .dominantDirection(dominantDirection)
                                                  .highestSeverity(highestSeverity)
                                                  .supportiveSignalCount(supportiveSignalCount)
                                                  .cautionarySignalCount(cautionarySignalCount)
                                                  .headwindSignalCount(headwindSignalCount)
                                                  .primarySignalCategory("MACRO")
                                                  .primarySignalTitle(primarySignalTitle)
                                                  .primarySignalDetail("detail")
                                                  .regimeSignalsPayload("[]")
                                                  .build();
    }
}
