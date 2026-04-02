package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextCompositePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisRiskFactor;
import com.aicoinassist.batch.domain.report.dto.AnalysisScenario;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentWindowSummary;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisRiskFactorType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisScenarioBias;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisTrendLabel;

import java.math.BigDecimal;
import java.util.List;

class AnalysisRiskScenarioFactory {

    private final AnalysisReportFormattingSupport formattingSupport;
    private final AnalysisIndicatorStateSupport indicatorStateSupport;
    private final AnalysisDerivativeContextSupport derivativeContextSupport;
    private final AnalysisMacroContextSupport macroContextSupport;
    private final AnalysisSentimentContextSupport sentimentContextSupport;
    private final AnalysisOnchainContextSupport onchainContextSupport;

    AnalysisRiskScenarioFactory(
            AnalysisReportFormattingSupport formattingSupport,
            AnalysisIndicatorStateSupport indicatorStateSupport,
            AnalysisDerivativeContextSupport derivativeContextSupport,
            AnalysisMacroContextSupport macroContextSupport,
            AnalysisSentimentContextSupport sentimentContextSupport,
            AnalysisOnchainContextSupport onchainContextSupport
    ) {
        this.formattingSupport = formattingSupport;
        this.indicatorStateSupport = indicatorStateSupport;
        this.derivativeContextSupport = derivativeContextSupport;
        this.macroContextSupport = macroContextSupport;
        this.sentimentContextSupport = sentimentContextSupport;
        this.onchainContextSupport = onchainContextSupport;
    }

    List<AnalysisRiskFactor> riskFactors(
            MarketIndicatorSnapshotEntity snapshot,
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            AnalysisExternalContextCompositePayload externalContextComposite
    ) {
        List<AnalysisRiskFactor> candidates = new java.util.ArrayList<>();

        if (snapshot.getRsi14().compareTo(new BigDecimal("70")) >= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.RSI_OVERHEATING,
                    "RSI overheating",
                    "RSI14 is above 70, so upside continuation can weaken quickly.",
                    List.of("RSI14 " + snapshot.getRsi14().stripTrailingZeros().toPlainString() + " is above the 70 threshold.")
            ));
        }

        if (snapshot.getRsi14().compareTo(new BigDecimal("30")) <= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.RSI_COMPRESSION,
                    "RSI compression",
                    "RSI14 is below 30, so downside can be stretched and whipsaws can increase.",
                    List.of("RSI14 " + snapshot.getRsi14().stripTrailingZeros().toPlainString() + " is below the 30 threshold.")
            ));
        }

        if (snapshot.getCurrentPrice().compareTo(snapshot.getBollingerUpperBand()) >= 0
                || snapshot.getCurrentPrice().compareTo(snapshot.getBollingerLowerBand()) <= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.BAND_EXTENSION,
                    "Band extension",
                    "가격이 볼린저 밴드 외곽에 위치해 있어 평균 회귀 위험이 커질 수 있습니다.",
                    List.of("현재 가격이 볼린저 밴드 외곽에 닿아 있습니다.")
            ));
        }

        if (indicatorStateSupport.atrRatio(snapshot).compareTo(new BigDecimal("3.00")) >= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.ELEVATED_VOLATILITY,
                    "Elevated volatility",
                    "ATR14 is more than 3% of price, so intraperiod swings can expand.",
                    List.of("ATR14 ratio is " + indicatorStateSupport.atrRatio(snapshot).setScale(2, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "% of price.")
            ));
        }

        if (derivativeContext != null && derivativeContext.lastFundingRate().abs().compareTo(new BigDecimal("0.0004")) >= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.FUNDING_SKEW,
                    "Funding skew",
                    "펀딩은 " + formattingSupport.fundingRatePercentage(derivativeContext.lastFundingRate())
                            + " 수준으로, 방향성 레버리지 쏠림을 시사할 수 있습니다.",
                    List.of("현재 펀딩 비율은 " + formattingSupport.fundingRatePercentage(derivativeContext.lastFundingRate()) + "입니다.")
            ));
        }

        if (derivativeContext != null && derivativeContext.markIndexBasisRate().abs().compareTo(new BigDecimal("0.05")) >= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.BASIS_EXPANSION,
                    "Basis expansion",
                    "마크-인덱스 베이시스는 " + formattingSupport.signedPercent(derivativeContext.markIndexBasisRate())
                            + "로, 선물 가격이 현물과 괴리를 보이고 있습니다.",
                    List.of("마크-인덱스 베이시스 비율은 " + formattingSupport.signedPercent(derivativeContext.markIndexBasisRate()) + "입니다.")
            ));
        }

        AnalysisDerivativeWindowSummary derivativeWindowSummary = derivativeContext == null
                ? null
                : derivativeContextSupport.primaryDerivativeWindowSummary(reportType, derivativeContext);
        if (derivativeWindowSummary != null
                && derivativeWindowSummary.currentOpenInterestVsAverage() != null
                && derivativeWindowSummary.currentOpenInterestVsAverage().abs().compareTo(new BigDecimal("0.20")) >= 0) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.OPEN_INTEREST_CROWDING,
                    "Open interest crowding",
                    "미결제약정은 대표 윈도우 평균 대비 " + formattingSupport.signedRatio(derivativeWindowSummary.currentOpenInterestVsAverage())
                            + " 수준으로 형성돼 있습니다.",
                    List.of("미결제약정은 평균 대비 " + formattingSupport.signedRatio(derivativeWindowSummary.currentOpenInterestVsAverage()) + "입니다.")
            ));
        }

        AnalysisSentimentWindowSummary primarySentimentWindowSummary = sentimentContext == null
                ? null
                : sentimentContextSupport.primaryWindowSummary(reportType, sentimentContext);
        if (sentimentContext != null && (
                sentimentContext.indexValue().compareTo(new BigDecimal("70")) >= 0
                        || (primarySentimentWindowSummary != null
                        && primarySentimentWindowSummary.currentIndexVsAverage() != null
                        && primarySentimentWindowSummary.currentIndexVsAverage().compareTo(new BigDecimal("0.15")) >= 0)
        )) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.SENTIMENT_GREED_EXTREME,
                    "Sentiment greed extreme",
                    "공포탐욕 지수는 " + sentimentContext.indexValue().stripTrailingZeros().toPlainString()
                            + " (" + sentimentContext.classification() + ")로, 저항 부근에서 추격 매수 위험이 커질 수 있습니다.",
                    List.of(
                            "공포탐욕 분류는 " + sentimentContext.classification() + "입니다.",
                            primarySentimentWindowSummary == null || primarySentimentWindowSummary.currentIndexVsAverage() == null
                                    ? "현재 심리는 높은 상태입니다."
                                    : primarySentimentWindowSummary.windowType().name()
                                    + " 심리는 평균 대비 "
                                    + formattingSupport.signedRatio(primarySentimentWindowSummary.currentIndexVsAverage()) + "."
                    )
            ));
        }

        if (sentimentContext != null && (
                sentimentContext.indexValue().compareTo(new BigDecimal("30")) <= 0
                        || (primarySentimentWindowSummary != null
                        && primarySentimentWindowSummary.currentIndexVsAverage() != null
                        && primarySentimentWindowSummary.currentIndexVsAverage().compareTo(new BigDecimal("-0.15")) <= 0)
        )) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.SENTIMENT_FEAR_EXTREME,
                    "Sentiment fear extreme",
                    "공포탐욕 지수는 " + sentimentContext.indexValue().stripTrailingZeros().toPlainString()
                            + " (" + sentimentContext.classification() + ")로, 반응성 매도와 변동 확대 가능성이 있습니다.",
                    List.of(
                            "공포탐욕 분류는 " + sentimentContext.classification() + "입니다.",
                            primarySentimentWindowSummary == null || primarySentimentWindowSummary.currentIndexVsAverage() == null
                                    ? "현재 심리는 위축된 상태입니다."
                                    : primarySentimentWindowSummary.windowType().name()
                                    + " 심리는 평균 대비 "
                                    + formattingSupport.signedRatio(primarySentimentWindowSummary.currentIndexVsAverage()) + "."
                    )
            ));
        }

        AnalysisMacroWindowSummary primaryMacroWindowSummary = macroContext == null
                ? null
                : macroContextSupport.primaryWindowSummary(reportType, macroContext);
        if (macroContext != null && (
                macroContext.dxyProxyValue().compareTo(new BigDecimal("120")) >= 0
                        || macroContext.us10yYieldValue().compareTo(new BigDecimal("4.50")) >= 0
                        || macroContext.usdKrwValue().compareTo(new BigDecimal("1450")) >= 0
                        || (primaryMacroWindowSummary != null
                        && (
                        primaryMacroWindowSummary.currentDxyProxyVsAverage().compareTo(new BigDecimal("0.01")) >= 0
                                || primaryMacroWindowSummary.currentUs10yYieldVsAverage().compareTo(new BigDecimal("0.03")) >= 0
                                || primaryMacroWindowSummary.currentUsdKrwVsAverage().compareTo(new BigDecimal("0.01")) >= 0
                ))
        )) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.MACRO_VOLATILITY,
                    "Macro volatility",
                    "거시 환경은 DXY "
                            + macroContext.dxyProxyValue().stripTrailingZeros().toPlainString()
                            + ", US10Y "
                            + macroContext.us10yYieldValue().stripTrailingZeros().toPlainString()
                            + ", USD/KRW "
                            + macroContext.usdKrwValue().stripTrailingZeros().toPlainString()
                            + " 조합으로 가상자산 위험 선호를 압박할 수 있습니다.",
                    List.of(
                            "DXY 프록시는 " + macroContext.dxyProxyValue().stripTrailingZeros().toPlainString() + "입니다.",
                            "미국 10년물 금리는 " + macroContext.us10yYieldValue().stripTrailingZeros().toPlainString() + "입니다.",
                            "USD/KRW는 " + macroContext.usdKrwValue().stripTrailingZeros().toPlainString() + "입니다.",
                            primaryMacroWindowSummary == null
                                    ? "거시 환경은 강한 편입니다."
                                    : primaryMacroWindowSummary.windowType().name()
                                    + " 기준 DXY는 평균 대비 "
                                    + formattingSupport.signedRatio(primaryMacroWindowSummary.currentDxyProxyVsAverage())
                                    + ", US10Y는 평균 대비 "
                                    + formattingSupport.signedRatio(primaryMacroWindowSummary.currentUs10yYieldVsAverage())
                                    + ", USD/KRW는 평균 대비 "
                                    + formattingSupport.signedRatio(primaryMacroWindowSummary.currentUsdKrwVsAverage()) + "."
                    )
            ));
        }

        AnalysisOnchainComparisonFact primaryOnchainFact = onchainContext == null
                || onchainContext.comparisonFacts() == null
                || onchainContext.comparisonFacts().isEmpty()
                ? null
                : onchainContext.comparisonFacts().get(0);
        AnalysisOnchainWindowSummary primaryOnchainWindowSummary = onchainContext == null
                ? null
                : onchainContextSupport.primaryWindowSummary(reportType, onchainContext);
        boolean anchorContraction = primaryOnchainFact != null
                && primaryOnchainFact.activeAddressChangeRate() != null
                && primaryOnchainFact.transactionCountChangeRate() != null
                && primaryOnchainFact.activeAddressChangeRate().compareTo(new BigDecimal("-0.10")) <= 0
                && primaryOnchainFact.transactionCountChangeRate().compareTo(new BigDecimal("-0.10")) <= 0;
        boolean windowContraction = primaryOnchainWindowSummary != null
                && primaryOnchainWindowSummary.currentActiveAddressVsAverage() != null
                && primaryOnchainWindowSummary.currentTransactionCountVsAverage() != null
                && primaryOnchainWindowSummary.currentActiveAddressVsAverage().compareTo(new BigDecimal("-0.10")) <= 0
                && primaryOnchainWindowSummary.currentTransactionCountVsAverage().compareTo(new BigDecimal("-0.10")) <= 0;
        if (anchorContraction || windowContraction) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.ONCHAIN_ACTIVITY_CONTRACTION,
                    "On-chain activity contraction",
                    primaryOnchainFact != null
                            ? primaryOnchainFact.reference().name()
                            + " 기준 활성 주소는 "
                            + formattingSupport.signedRatio(primaryOnchainFact.activeAddressChangeRate())
                            + ", 트랜잭션은 "
                            + formattingSupport.signedRatio(primaryOnchainFact.transactionCountChangeRate())
                            + " 수준으로 기초 네트워크 참여 약화를 시사할 수 있습니다."
                            : primaryOnchainWindowSummary.windowType().name()
                            + " 기준 활성 주소는 "
                            + formattingSupport.signedRatio(primaryOnchainWindowSummary.currentActiveAddressVsAverage())
                            + ", 트랜잭션은 "
                            + formattingSupport.signedRatio(primaryOnchainWindowSummary.currentTransactionCountVsAverage())
                            + " 수준으로 네트워크 참여 약화를 시사할 수 있습니다.",
                    List.of(
                            primaryOnchainFact == null
                                    ? primaryOnchainWindowSummary.windowType().name()
                                    + " 기준 활성 주소는 평균 대비 "
                                    + formattingSupport.signedRatio(primaryOnchainWindowSummary.currentActiveAddressVsAverage()) + "."
                                    : primaryOnchainFact.reference().name() + " 대비 활성 주소는 "
                                    + formattingSupport.signedRatio(primaryOnchainFact.activeAddressChangeRate()) + ".",
                            primaryOnchainFact == null
                                    ? primaryOnchainWindowSummary.windowType().name()
                                    + " 기준 트랜잭션 수는 평균 대비 "
                                    + formattingSupport.signedRatio(primaryOnchainWindowSummary.currentTransactionCountVsAverage()) + "."
                                    : primaryOnchainFact.reference().name() + " 대비 트랜잭션 수는 "
                                    + formattingSupport.signedRatio(primaryOnchainFact.transactionCountChangeRate()) + "."
                    )
            ));
        }

        if (externalContextComposite != null
                && externalContextComposite.compositeRiskScore() != null
                && externalContextComposite.compositeRiskScore().compareTo(new BigDecimal("1.00")) >= 0
                && externalContextComposite.primarySignalTitle() != null) {
            String windowSummaryDetail = externalContextComposite.windowSummaries() == null
                    || externalContextComposite.windowSummaries().isEmpty()
                    || externalContextComposite.windowSummaries().get(0).currentCompositeRiskVsAverage() == null
                    ? null
                    : externalContextComposite.windowSummaries().get(0).windowType().name()
                    + " 외부 복합 리스크는 평균 대비 "
                    + formattingSupport.signedRatio(externalContextComposite.windowSummaries().get(0).currentCompositeRiskVsAverage()) + "입니다.";
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.EXTERNAL_RISK_CONFLUENCE,
                    "External risk confluence",
                    "외부 복합 국면은 "
                            + externalContextComposite.primarySignalTitle()
                            + " 신호를 중심으로 리스크 점수 "
                            + externalContextComposite.compositeRiskScore().stripTrailingZeros().toPlainString()
                            + "를 유지하고 있습니다.",
                    java.util.stream.Stream.of(
                                    java.util.stream.Stream.of(
                                            "지배적인 외부 방향은 "
                                                    + (externalContextComposite.dominantDirection() == null
                                                    ? "mixed"
                                                    : externalContextComposite.dominantDirection().name().toLowerCase().replace('_', ' '))
                                                    + "입니다."
                                    ),
                                    windowSummaryDetail == null
                                            ? java.util.stream.Stream.<String>empty()
                                            : java.util.stream.Stream.of(windowSummaryDetail),
                                    externalContextComposite.persistence() == null
                                            ? java.util.stream.Stream.<String>empty()
                                            : java.util.stream.Stream.of(externalContextComposite.persistence().summary()),
                                    externalContextComposite.state() == null
                                            ? java.util.stream.Stream.<String>empty()
                                            : java.util.stream.Stream.of(
                                            "외부 반전 리스크 점수는 "
                                                    + externalContextComposite.state().reversalRiskScore().setScale(2, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
                                                    + "입니다."
                                    ),
                                    externalContextComposite.highlights() == null
                                            ? java.util.stream.Stream.<String>empty()
                                            : externalContextComposite.highlights().stream()
                                                                      .limit(2)
                                                                      .map(AnalysisExternalContextHighlight::summary)
                            )
                            .flatMap(java.util.function.Function.identity())
                            .toList()
            ));
        }

        if (candidates.isEmpty()) {
            candidates.add(new AnalysisRiskFactor(
                    AnalysisRiskFactorType.MOMENTUM_TRANSITION,
                    "Momentum transition",
                    "모멘텀이 한쪽으로 확정되지 않아 핵심 레벨 부근에서 추세 연장이 둔화될 수 있습니다.",
                    List.of("현재 신호가 혼재돼 있어 인접 레벨 부근에서 흐름이 멈출 수 있습니다.")
            ));
        }

        return candidates;
    }

    List<AnalysisScenario> scenarios(
            MarketIndicatorSnapshotEntity snapshot,
            AnalysisTrendLabel trendBias,
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            AnalysisExternalContextCompositePayload externalContextComposite
    ) {
        List<String> externalTriggers = externalTriggerConditions(
                reportType,
                derivativeContext,
                macroContext,
                sentimentContext,
                onchainContext,
                externalContextComposite
        );
        List<String> externalInvalidations = externalInvalidationSignals(
                reportType,
                derivativeContext,
                macroContext,
                sentimentContext,
                onchainContext,
                externalContextComposite
        );
        String externalPath = externalPathSummary(
                reportType,
                derivativeContext,
                macroContext,
                sentimentContext,
                onchainContext,
                externalContextComposite
        );

        return switch (trendBias) {
            case BULLISH -> List.of(
                    new AnalysisScenario(
                            "Base case",
                            AnalysisScenarioBias.BULLISH,
                            appendAll(List.of("가격이 MA20 위를 유지합니다.", "모멘텀은 우호적인 상태를 유지합니다."), externalTriggers),
                            "가격이 MA20 위를 유지하며 "
                                    + snapshot.getBollingerUpperBand().stripTrailingZeros().toPlainString()
                                    + " 부근까지 확장될 수 있습니다. " + externalPath,
                            appendAll(List.of("MA20 이탈 시 상승 지속 경로는 약해집니다."), externalInvalidations)
                    ),
                    new AnalysisScenario(
                            "Risk case",
                            AnalysisScenarioBias.NEUTRAL,
                            appendAll(List.of("가격이 MA20 지지를 이탈합니다."), externalTriggers),
                            "MA20 이탈 시 "
                                    + snapshot.getMa60().stripTrailingZeros().toPlainString()
                                    + " 부근까지 되돌림이 전개될 수 있습니다. "
                                    + externalRiskPathSummary(reportType, macroContext, sentimentContext, onchainContext, externalContextComposite),
                            appendAll(List.of("빠르게 MA20을 회복하면 되돌림 시나리오는 무효화됩니다."), externalInvalidations)
                    )
            );
            case BEARISH -> List.of(
                    new AnalysisScenario(
                            "Base case",
                            AnalysisScenarioBias.BEARISH,
                            appendAll(List.of("가격이 MA20 아래에 머뭅니다.", "모멘텀은 약한 상태입니다."), externalTriggers),
                            "가격이 MA20 아래에 머물며 "
                                    + snapshot.getBollingerLowerBand().stripTrailingZeros().toPlainString()
                                    + " 부근을 재차 시험할 수 있습니다. "
                                    + externalRiskPathSummary(reportType, macroContext, sentimentContext, onchainContext, externalContextComposite),
                            appendAll(List.of("MA20 회복 시 하락 지속 시나리오는 약해집니다."), externalInvalidations)
                    ),
                    new AnalysisScenario(
                            "Risk case",
                            AnalysisScenarioBias.NEUTRAL,
                            appendAll(List.of("가격이 MA20을 회복합니다."), externalTriggers),
                            "가격이 MA20을 회복하면 "
                                    + snapshot.getMa60().stripTrailingZeros().toPlainString()
                                    + " 부근까지 숏커버링이 전개될 수 있습니다. " + externalPath,
                            appendAll(List.of("다시 MA20 아래로 밀리면 숏커버링 시나리오는 무효화됩니다."), externalInvalidations)
                    )
            );
            case NEUTRAL -> List.of(
                    new AnalysisScenario(
                            "Base case",
                            AnalysisScenarioBias.NEUTRAL,
                            appendAll(List.of("가격이 현재 활성 범위 안에 머뭅니다.", "추세 강도는 혼재된 상태입니다."), externalTriggers),
                            "가격은 방향 확인 전까지 지지와 저항 사이에서 등락할 가능성이 큽니다. " + externalPath,
                            appendAll(List.of("범위 상하단을 분명하게 돌파하면 박스권 시나리오는 무효화됩니다."), externalInvalidations)
                    ),
                    new AnalysisScenario(
                            "Breakout case",
                            AnalysisScenarioBias.DIRECTIONAL,
                            appendAll(List.of("가격이 현재 밴드 상하단을 돌파합니다."), externalTriggers),
                            "현재 밴드 극단값을 분명하게 이탈하면 다음 단기 방향이 정해질 수 있습니다. " + externalPath,
                            appendAll(List.of("돌파 레벨을 지키지 못하면 방향성 시나리오는 무효화됩니다."), externalInvalidations)
                    )
            );
        };
    }

    private List<String> externalTriggerConditions(
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            AnalysisExternalContextCompositePayload externalContextComposite
    ) {
        java.util.ArrayList<String> triggers = new java.util.ArrayList<>();

        AnalysisDerivativeWindowSummary derivativeWindowSummary = derivativeContext == null
                ? null
                : derivativeContextSupport.primaryDerivativeWindowSummary(reportType, derivativeContext);
        if (derivativeWindowSummary != null && derivativeWindowSummary.currentOpenInterestVsAverage() != null
                && derivativeWindowSummary.currentOpenInterestVsAverage().abs().compareTo(new BigDecimal("0.20")) >= 0) {
            triggers.add(derivativeWindowSummary.windowType().name() + " 미결제약정은 평균 대비 "
                    + formattingSupport.signedRatio(derivativeWindowSummary.currentOpenInterestVsAverage())
                    + " 수준을 유지합니다.");
        }

        AnalysisMacroWindowSummary macroWindowSummary = macroContext == null
                ? null
                : macroContextSupport.primaryWindowSummary(reportType, macroContext);
        if (macroWindowSummary != null && (
                macroWindowSummary.currentDxyProxyVsAverage().compareTo(new BigDecimal("0.01")) >= 0
                        || macroWindowSummary.currentUs10yYieldVsAverage().compareTo(new BigDecimal("0.03")) >= 0
                        || macroWindowSummary.currentUsdKrwVsAverage().compareTo(new BigDecimal("0.01")) >= 0
        )) {
            triggers.add(macroWindowSummary.windowType().name() + " macro backdrop still shows firm dollar/yield pressure.");
        }

        AnalysisSentimentWindowSummary sentimentWindowSummary = sentimentContext == null
                ? null
                : sentimentContextSupport.primaryWindowSummary(reportType, sentimentContext);
        if (sentimentWindowSummary != null && sentimentWindowSummary.currentIndexVsAverage() != null
                && sentimentWindowSummary.currentIndexVsAverage().abs().compareTo(new BigDecimal("0.15")) >= 0) {
            triggers.add(sentimentWindowSummary.windowType().name() + " 심리는 평균 대비 "
                    + formattingSupport.signedRatio(sentimentWindowSummary.currentIndexVsAverage())
                    + " 수준을 유지합니다.");
        }

        AnalysisOnchainWindowSummary onchainWindowSummary = onchainContext == null
                ? null
                : onchainContextSupport.primaryWindowSummary(reportType, onchainContext);
        if (onchainWindowSummary != null
                && onchainWindowSummary.currentActiveAddressVsAverage() != null
                && onchainWindowSummary.currentTransactionCountVsAverage() != null) {
            if (onchainWindowSummary.currentActiveAddressVsAverage().compareTo(new BigDecimal("0.10")) >= 0
                    && onchainWindowSummary.currentTransactionCountVsAverage().compareTo(new BigDecimal("0.10")) >= 0) {
                triggers.add(onchainWindowSummary.windowType().name() + " 온체인 활동은 평균을 웃도는 상태입니다.");
            } else if (onchainWindowSummary.currentActiveAddressVsAverage().compareTo(new BigDecimal("-0.10")) <= 0
                    && onchainWindowSummary.currentTransactionCountVsAverage().compareTo(new BigDecimal("-0.10")) <= 0) {
                triggers.add(onchainWindowSummary.windowType().name() + " 온체인 활동은 평균을 밑도는 상태입니다.");
            }
        }

        if (externalContextComposite != null && externalContextComposite.highlights() != null) {
            externalContextComposite.highlights().stream()
                                    .limit(2)
                                    .map(AnalysisExternalContextHighlight::summary)
                                    .forEach(triggers::add);
        }
        if (externalContextComposite != null && externalContextComposite.transitions() != null) {
            externalContextComposite.transitions().stream()
                                    .limit(1)
                                    .map(transition -> transition.summary())
                                    .forEach(triggers::add);
        }
        if (externalContextComposite != null
                && externalContextComposite.windowSummaries() != null
                && !externalContextComposite.windowSummaries().isEmpty()
                && externalContextComposite.windowSummaries().get(0).currentCompositeRiskVsAverage() != null
                && externalContextComposite.windowSummaries().get(0).currentCompositeRiskVsAverage().compareTo(new BigDecimal("0.15")) >= 0) {
            triggers.add(externalContextComposite.windowSummaries().get(0).windowType().name()
                    + " 외부 복합 리스크는 평균 대비 "
                    + formattingSupport.signedRatio(externalContextComposite.windowSummaries().get(0).currentCompositeRiskVsAverage())
                    + " 수준으로 유지됩니다.");
        }
        if (externalContextComposite != null
                && externalContextComposite.state() != null
                && externalContextComposite.state().reversalRiskScore().compareTo(new BigDecimal("0.55")) >= 0) {
            triggers.add("외부 국면 반전 리스크는 "
                    + externalContextComposite.state().reversalRiskScore().setScale(2, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
                    + " 수준으로 높게 유지됩니다.");
        }

        return triggers;
    }

    private String externalPathSummary(
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            AnalysisExternalContextCompositePayload externalContextComposite
    ) {
        java.util.ArrayList<String> clauses = new java.util.ArrayList<>();

        AnalysisSentimentWindowSummary sentimentWindowSummary = sentimentContext == null
                ? null
                : sentimentContextSupport.primaryWindowSummary(reportType, sentimentContext);
        if (sentimentContext != null && sentimentWindowSummary != null && sentimentWindowSummary.currentIndexVsAverage() != null) {
            clauses.add("심리는 평균 대비 " + formattingSupport.signedRatio(sentimentWindowSummary.currentIndexVsAverage()) + " 수준입니다");
        }

        AnalysisMacroWindowSummary macroWindowSummary = macroContext == null
                ? null
                : macroContextSupport.primaryWindowSummary(reportType, macroContext);
        if (macroWindowSummary != null) {
            clauses.add("거시 환경에서 DXY는 평균 대비 " + formattingSupport.signedRatio(macroWindowSummary.currentDxyProxyVsAverage()) + " 수준입니다");
        }

        AnalysisOnchainWindowSummary onchainWindowSummary = onchainContext == null
                ? null
                : onchainContextSupport.primaryWindowSummary(reportType, onchainContext);
        if (onchainWindowSummary != null) {
            clauses.add("온체인 활동은 평균 대비 " + formattingSupport.signedRatio(onchainWindowSummary.currentActiveAddressVsAverage()) + " 수준입니다");
        }

        if (externalContextComposite != null && externalContextComposite.primarySignalTitle() != null) {
            clauses.add("외부 국면의 중심 신호는 " + externalContextComposite.primarySignalTitle() + "입니다");
        }
        if (externalContextComposite != null && externalContextComposite.persistence() != null) {
            clauses.add(externalContextComposite.persistence().summary());
        }
        if (externalContextComposite != null
                && externalContextComposite.windowSummaries() != null
                && !externalContextComposite.windowSummaries().isEmpty()
                && externalContextComposite.windowSummaries().get(0).currentCompositeRiskVsAverage() != null) {
            clauses.add("외부 복합 리스크는 대표 평균 대비 "
                    + formattingSupport.signedRatio(externalContextComposite.windowSummaries().get(0).currentCompositeRiskVsAverage())
                    + " 수준입니다");
        }
        if (externalContextComposite != null && externalContextComposite.state() != null) {
            clauses.add("반전 리스크는 "
                    + externalContextComposite.state().reversalRiskScore().setScale(2, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
        }

        return clauses.isEmpty() ? "외부 컨텍스트는 혼재된 상태입니다." : String.join(", ", clauses) + ".";
    }

    private String externalRiskPathSummary(
            AnalysisReportType reportType,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            AnalysisExternalContextCompositePayload externalContextComposite
    ) {
        java.util.ArrayList<String> clauses = new java.util.ArrayList<>();

        AnalysisMacroWindowSummary macroWindowSummary = macroContext == null
                ? null
                : macroContextSupport.primaryWindowSummary(reportType, macroContext);
        if (macroWindowSummary != null && (
                macroWindowSummary.currentDxyProxyVsAverage().compareTo(new BigDecimal("0.01")) >= 0
                        || macroWindowSummary.currentUs10yYieldVsAverage().compareTo(new BigDecimal("0.03")) >= 0
        )) {
            clauses.add("Firm macro conditions can amplify downside volatility");
        }

        AnalysisSentimentWindowSummary sentimentWindowSummary = sentimentContext == null
                ? null
                : sentimentContextSupport.primaryWindowSummary(reportType, sentimentContext);
        if (sentimentWindowSummary != null && sentimentWindowSummary.currentIndexVsAverage() != null
                && sentimentWindowSummary.currentIndexVsAverage().compareTo(new BigDecimal("0.15")) >= 0) {
            clauses.add("elevated greed can unwind quickly");
        }

        AnalysisOnchainWindowSummary onchainWindowSummary = onchainContext == null
                ? null
                : onchainContextSupport.primaryWindowSummary(reportType, onchainContext);
        if (onchainWindowSummary != null
                && onchainWindowSummary.currentActiveAddressVsAverage().compareTo(new BigDecimal("-0.10")) <= 0
                && onchainWindowSummary.currentTransactionCountVsAverage().compareTo(new BigDecimal("-0.10")) <= 0) {
            clauses.add("soft on-chain activity can weaken follow-through");
        }

        if (externalContextComposite != null
                && externalContextComposite.compositeRiskScore() != null
                && externalContextComposite.compositeRiskScore().compareTo(new BigDecimal("1.00")) >= 0) {
            clauses.add("외부 국면의 신호 결합으로 리스크 편향이 높게 유지됩니다");
        }
        if (externalContextComposite != null
                && externalContextComposite.persistence() != null
                && externalContextComposite.persistence().persistenceScore().compareTo(new BigDecimal("0.60")) >= 0) {
            clauses.add("외부 국면의 지속성이 같은 편향을 고착시키고 있습니다");
        }
        if (externalContextComposite != null
                && externalContextComposite.windowSummaries() != null
                && !externalContextComposite.windowSummaries().isEmpty()
                && externalContextComposite.windowSummaries().get(0).currentCompositeRiskVsAverage() != null
                && externalContextComposite.windowSummaries().get(0).currentCompositeRiskVsAverage().compareTo(new BigDecimal("0.15")) >= 0) {
            clauses.add("외부 복합 리스크가 대표 평균보다 높은 상태입니다");
        }
        if (externalContextComposite != null
                && externalContextComposite.state() != null
                && externalContextComposite.state().reversalRiskScore().compareTo(new BigDecimal("0.55")) >= 0) {
            clauses.add("외부 국면 반전 리스크가 높은 수준을 유지합니다");
        }

        return clauses.isEmpty() ? "외부 컨텍스트는 뚜렷한 리스크 편향을 추가하지 않습니다." : String.join(", ", clauses) + ".";
    }

    private List<String> externalInvalidationSignals(
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            AnalysisExternalContextCompositePayload externalContextComposite
    ) {
        java.util.ArrayList<String> signals = new java.util.ArrayList<>();

        AnalysisDerivativeWindowSummary derivativeWindowSummary = derivativeContext == null
                ? null
                : derivativeContextSupport.primaryDerivativeWindowSummary(reportType, derivativeContext);
        if (derivativeWindowSummary != null && derivativeWindowSummary.currentFundingVsAverage() != null) {
            signals.add("펀딩과 미결제약정은 대표 평균에 가까워지며 정상화됩니다.");
        }

        AnalysisMacroWindowSummary macroWindowSummary = macroContext == null
                ? null
                : macroContextSupport.primaryWindowSummary(reportType, macroContext);
        if (macroWindowSummary != null) {
            signals.add("달러와 금리 압력은 윈도우 평균 방향으로 완화됩니다.");
        }

        AnalysisSentimentWindowSummary sentimentWindowSummary = sentimentContext == null
                ? null
                : sentimentContextSupport.primaryWindowSummary(reportType, sentimentContext);
        if (sentimentWindowSummary != null) {
            signals.add("공포탐욕 지수는 최근 평균 방향으로 되돌아갑니다.");
        }

        AnalysisOnchainWindowSummary onchainWindowSummary = onchainContext == null
                ? null
                : onchainContextSupport.primaryWindowSummary(reportType, onchainContext);
        if (onchainWindowSummary != null) {
            signals.add("온체인 활동은 최근 평균 부근에서 안정됩니다.");
        }

        if (externalContextComposite != null) {
            signals.add("외부 복합 국면 점수가 다시 중립에 가까워집니다.");
            if (externalContextComposite.windowSummaries() != null && !externalContextComposite.windowSummaries().isEmpty()) {
                signals.add("외부 복합 리스크가 대표 윈도우 평균 방향으로 정상화됩니다.");
            }
            if (externalContextComposite.persistence() != null) {
                signals.add("외부 국면의 지속성이 약해지며 현재 편향 강화가 멈춥니다.");
            }
            if (externalContextComposite.state() != null) {
                signals.add("외부 반전 리스크 점수가 더 낮은 구간으로 내려옵니다.");
            }
        }

        return signals;
    }

    private List<String> appendAll(List<String> base, List<String> extras) {
        java.util.ArrayList<String> merged = new java.util.ArrayList<>(base);
        merged.addAll(extras);
        return merged;
    }
}
