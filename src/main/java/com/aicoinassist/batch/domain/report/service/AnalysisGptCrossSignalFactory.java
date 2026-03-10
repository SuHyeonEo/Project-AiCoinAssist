package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextCompositePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisGptCrossSignal;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentWindowSummary;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeDirection;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisGptCrossSignalCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisGptSignalBias;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AnalysisGptCrossSignalFactory {

    public List<AnalysisGptCrossSignal> build(AnalysisReportPayload payload) {
        List<AnalysisGptCrossSignal> signals = new ArrayList<>();

        macroDerivativeSignal(payload).ifPresent(signals::add);
        sentimentDerivativeSignal(payload).ifPresent(signals::add);
        onchainMacroSignal(payload).ifPresent(signals::add);
        externalLevelSignal(payload).ifPresent(signals::add);
        breadthBreakoutSignal(payload).ifPresent(signals::add);

        return signals.stream()
                .sorted(Comparator.comparingInt(AnalysisGptCrossSignal::strengthScore).reversed())
                .toList();
    }

    private java.util.Optional<AnalysisGptCrossSignal> macroDerivativeSignal(AnalysisReportPayload payload) {
        AnalysisMacroWindowSummary macroWindow = firstWindow(payload.macroContext() == null ? null : payload.macroContext().windowSummaries());
        AnalysisDerivativeWindowSummary derivativeWindow = firstWindow(payload.derivativeContext() == null ? null : payload.derivativeContext().windowSummaries());
        if (macroWindow == null || derivativeWindow == null) {
            return java.util.Optional.empty();
        }

        boolean macroHeadwind = positive(macroWindow.currentDxyProxyVsAverage(), "0.005")
                || positive(macroWindow.currentUs10yYieldVsAverage(), "0.020")
                || positive(macroWindow.currentUsdKrwVsAverage(), "0.010");
        boolean crowdedLeverage = positive(derivativeWindow.currentFundingVsAverage(), "0.500")
                || positive(derivativeWindow.currentBasisVsAverage(), "0.500");
        if (!macroHeadwind || !crowdedLeverage) {
            return java.util.Optional.empty();
        }

        return java.util.Optional.of(new AnalysisGptCrossSignal(
                AnalysisGptCrossSignalCategory.MACRO_DERIVATIVE,
                "Macro pressure with crowded leverage",
                AnalysisGptSignalBias.HEADWIND,
                5,
                List.of(
                        "DXY vs average " + signedPercent(macroWindow.currentDxyProxyVsAverage()),
                        "US10Y vs average " + signedPercent(macroWindow.currentUs10yYieldVsAverage()),
                        "Funding vs average " + signedPercent(derivativeWindow.currentFundingVsAverage()),
                        "Basis vs average " + signedPercent(derivativeWindow.currentBasisVsAverage())
                ),
                "Macro headwind and leveraged positioning are aligned, which raises unwind risk for crypto trend continuation."
        ));
    }

    private java.util.Optional<AnalysisGptCrossSignal> sentimentDerivativeSignal(AnalysisReportPayload payload) {
        AnalysisSentimentContext sentimentContext = payload.sentimentContext();
        AnalysisDerivativeWindowSummary derivativeWindow = firstWindow(payload.derivativeContext() == null ? null : payload.derivativeContext().windowSummaries());
        if (sentimentContext == null || derivativeWindow == null) {
            return java.util.Optional.empty();
        }
        boolean greed = sentimentContext.indexValue() != null && sentimentContext.indexValue().compareTo(new BigDecimal("65")) >= 0;
        boolean crowdedLeverage = positive(derivativeWindow.currentFundingVsAverage(), "0.500");
        if (!greed || !crowdedLeverage) {
            return java.util.Optional.empty();
        }

        return java.util.Optional.of(new AnalysisGptCrossSignal(
                AnalysisGptCrossSignalCategory.SENTIMENT_DERIVATIVE,
                "Greed with crowded leverage",
                AnalysisGptSignalBias.CAUTIONARY,
                4,
                List.of(
                        "Fear & Greed index " + plain(sentimentContext.indexValue()),
                        "Funding vs average " + signedPercent(derivativeWindow.currentFundingVsAverage()),
                        "Classification " + sentimentContext.classification()
                ),
                "Greedy sentiment is sitting on top of elevated funding, which can make the short-term market more fragile."
        ));
    }

    private java.util.Optional<AnalysisGptCrossSignal> onchainMacroSignal(AnalysisReportPayload payload) {
        AnalysisOnchainWindowSummary onchainWindow = firstWindow(payload.onchainContext() == null ? null : payload.onchainContext().windowSummaries());
        AnalysisMacroWindowSummary macroWindow = firstWindow(payload.macroContext() == null ? null : payload.macroContext().windowSummaries());
        if (onchainWindow == null || macroWindow == null) {
            return java.util.Optional.empty();
        }
        boolean onchainStrong = positive(onchainWindow.currentActiveAddressVsAverage(), "0.050")
                || positive(onchainWindow.currentTransactionCountVsAverage(), "0.050");
        boolean macroHeadwind = positive(macroWindow.currentDxyProxyVsAverage(), "0.005")
                || positive(macroWindow.currentUs10yYieldVsAverage(), "0.020");
        if (!onchainStrong || !macroHeadwind) {
            return java.util.Optional.empty();
        }

        return java.util.Optional.of(new AnalysisGptCrossSignal(
                AnalysisGptCrossSignalCategory.ONCHAIN_MACRO,
                "On-chain resilience against macro drag",
                AnalysisGptSignalBias.MIXED,
                3,
                List.of(
                        "Active addresses vs average " + signedPercent(onchainWindow.currentActiveAddressVsAverage()),
                        "Transactions vs average " + signedPercent(onchainWindow.currentTransactionCountVsAverage()),
                        "DXY vs average " + signedPercent(macroWindow.currentDxyProxyVsAverage())
                ),
                "Macro pressure is still present, but on-chain participation remains firm enough to suggest underlying network demand is holding up."
        ));
    }

    private java.util.Optional<AnalysisGptCrossSignal> externalLevelSignal(AnalysisReportPayload payload) {
        AnalysisExternalContextCompositePayload external = payload.marketContext() == null ? null : payload.marketContext().externalContextComposite();
        if (external == null || payload.marketContext().levelContext() == null) {
            return java.util.Optional.empty();
        }
        boolean headwind = external.state() != null
                ? external.state().dominantDirection() == AnalysisExternalRegimeDirection.HEADWIND
                : external.dominantDirection() == AnalysisExternalRegimeDirection.HEADWIND;
        if (!headwind || !positive(payload.marketContext().levelContext().supportBreakRisk(), "0.150")) {
            return java.util.Optional.empty();
        }

        return java.util.Optional.of(new AnalysisGptCrossSignal(
                AnalysisGptCrossSignalCategory.EXTERNAL_LEVEL,
                "Support fragility under external headwind",
                AnalysisGptSignalBias.HEADWIND,
                4,
                List.of(
                        "Support break risk " + signedPercent(payload.marketContext().levelContext().supportBreakRisk()),
                        "External dominant direction " + (external.state() != null
                                ? external.state().dominantDirection().name()
                                : external.dominantDirection().name()),
                        external.state() != null ? external.state().summary() : external.primarySignalDetail()
                ),
                "Support is more vulnerable because external conditions remain headwind-heavy while downside level risk is already elevated."
        ));
    }

    private java.util.Optional<AnalysisGptCrossSignal> breadthBreakoutSignal(AnalysisReportPayload payload) {
        AnalysisOnchainWindowSummary onchainWindow = firstWindow(payload.onchainContext() == null ? null : payload.onchainContext().windowSummaries());
        AnalysisSentimentContext sentimentContext = payload.sentimentContext();
        AnalysisExternalContextCompositePayload external = payload.marketContext() == null ? null : payload.marketContext().externalContextComposite();
        if (payload.marketContext() == null || payload.marketContext().levelContext() == null
                || onchainWindow == null || sentimentContext == null || external == null) {
            return java.util.Optional.empty();
        }

        boolean breadthPositive = positive(onchainWindow.currentActiveAddressVsAverage(), "0.050")
                && sentimentContext.indexValue() != null
                && sentimentContext.indexValue().compareTo(new BigDecimal("55")) >= 0;
        boolean externalNotHeadwind = external.state() != null
                ? external.state().dominantDirection() != AnalysisExternalRegimeDirection.HEADWIND
                : external.dominantDirection() != AnalysisExternalRegimeDirection.HEADWIND;
        boolean breakoutPressure = positive(payload.marketContext().levelContext().resistanceBreakRisk(), "0.050");
        if (!breadthPositive || !externalNotHeadwind || !breakoutPressure) {
            return java.util.Optional.empty();
        }

        return java.util.Optional.of(new AnalysisGptCrossSignal(
                AnalysisGptCrossSignalCategory.BREADTH_BREAKOUT,
                "Breadth supports resistance breakout",
                AnalysisGptSignalBias.SUPPORTIVE,
                3,
                List.of(
                        "Resistance break risk " + signedPercent(payload.marketContext().levelContext().resistanceBreakRisk()),
                        "Active addresses vs average " + signedPercent(onchainWindow.currentActiveAddressVsAverage()),
                        "Fear & Greed index " + plain(sentimentContext.indexValue())
                ),
                "Broad participation and resilient risk appetite are giving the market a better chance to challenge the nearest resistance zone."
        ));
    }

    private <T> T firstWindow(List<T> summaries) {
        return summaries == null || summaries.isEmpty() ? null : summaries.get(0);
    }

    private boolean positive(BigDecimal value, String threshold) {
        return value != null && value.compareTo(new BigDecimal(threshold)) >= 0;
    }

    private String signedPercent(BigDecimal ratio) {
        return ratio == null
                ? "unavailable"
                : ratio.multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString() + "%";
    }

    private String plain(BigDecimal value) {
        return value == null ? "unavailable" : value.stripTrailingZeros().toPlainString();
    }
}
