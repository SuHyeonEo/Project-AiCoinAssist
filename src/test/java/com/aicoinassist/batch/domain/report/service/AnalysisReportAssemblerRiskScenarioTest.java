package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisRiskFactorType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisScenarioBias;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisReportAssemblerRiskScenarioTest extends AnalysisReportServiceFixtures {

    private final AnalysisReportAssembler assembler = new AnalysisReportAssembler();

    @Test
    void assembleAddsRiskFactorsAndScenarioDetailsWhenSnapshotShowsExtension() {
        AnalysisReportPayload payload = assembler.assemble(
                extendedSnapshot(),
                AnalysisReportType.SHORT_TERM,
                comparisonFacts(),
                shortWindowSummaries(),
                derivativeContext(),
                macroContext(),
                sentimentContext(),
                onchainContext(),
                shortContinuityNotes(),
                richExternalContextComposite(),
                levelContext(),
                supportLevels(),
                resistanceLevels(),
                supportZones(),
                resistanceZones()
        );

        assertThat(payload.riskFactors()).extracting("type", "title")
                                         .contains(
                                                 org.assertj.core.groups.Tuple.tuple(AnalysisRiskFactorType.RSI_OVERHEATING, "RSI overheating"),
                                                 org.assertj.core.groups.Tuple.tuple(AnalysisRiskFactorType.BAND_EXTENSION, "Band extension"),
                                                 org.assertj.core.groups.Tuple.tuple(AnalysisRiskFactorType.ELEVATED_VOLATILITY, "Elevated volatility"),
                                                 org.assertj.core.groups.Tuple.tuple(AnalysisRiskFactorType.FUNDING_SKEW, "Funding skew"),
                                                 org.assertj.core.groups.Tuple.tuple(AnalysisRiskFactorType.BASIS_EXPANSION, "Basis expansion"),
                                                 org.assertj.core.groups.Tuple.tuple(AnalysisRiskFactorType.SENTIMENT_GREED_EXTREME, "Sentiment greed extreme"),
                                                 org.assertj.core.groups.Tuple.tuple(AnalysisRiskFactorType.MACRO_VOLATILITY, "Macro volatility"),
                                                 org.assertj.core.groups.Tuple.tuple(AnalysisRiskFactorType.EXTERNAL_RISK_CONFLUENCE, "External risk confluence")
                                         );
        assertThat(payload.riskFactors()).allSatisfy(riskFactor -> assertThat(riskFactor.triggerFacts()).isNotEmpty());
        assertThat(payload.scenarios()).extracting("bias").contains(AnalysisScenarioBias.BULLISH, AnalysisScenarioBias.NEUTRAL);
        assertThat(payload.scenarios()).allSatisfy(scenario -> {
            assertThat(scenario.triggerConditions()).isNotEmpty();
            assertThat(scenario.pathSummary()).isNotBlank();
            assertThat(scenario.invalidationSignals()).isNotEmpty();
        });
        assertThat(payload.marketContext().externalRegimeSignals()).isNotEmpty();
        assertThat(payload.scenarios().get(0).triggerConditions()).anySatisfy(condition -> assertThat(condition).contains("macro backdrop"));
        assertThat(payload.scenarios().get(0).triggerConditions()).anySatisfy(condition -> assertThat(condition).contains("sentiment remains"));
        assertThat(payload.scenarios().get(0).triggerConditions()).anySatisfy(condition -> assertThat(condition).contains("external regime"));
        assertThat(payload.scenarios().get(0).triggerConditions()).anySatisfy(condition -> assertThat(condition).contains("reversal risk"));
        assertThat(payload.scenarios().get(0).pathSummary()).contains("Sentiment remains");
        assertThat(payload.scenarios().get(0).pathSummary()).contains("external regime focus");
        assertThat(payload.riskFactors()).anySatisfy(riskFactor -> {
            if (riskFactor.type() == AnalysisRiskFactorType.EXTERNAL_RISK_CONFLUENCE) {
                assertThat(riskFactor.triggerFacts()).anySatisfy(trigger -> assertThat(trigger).contains("reversal risk score"));
                assertThat(riskFactor.triggerFacts()).anySatisfy(trigger -> assertThat(trigger).contains("headwind"));
            }
        });
    }
}
