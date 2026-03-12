package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.entity.MarketIndicatorSnapshotEntity;
import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextCompositePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisLevelContextPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisSummaryKeyMessagePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisSummaryPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.dto.AnalysisZoneInteractionFact;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisOutlookType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisTrendLabel;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

class AnalysisSummarySectionAssembler {

    private final AnalysisIndicatorStateSupport indicatorStateSupport;
    private final AnalysisComparisonWindowSupport comparisonWindowSupport;
    private final AnalysisDerivativeContextSupport derivativeContextSupport;
    private final AnalysisMacroContextSupport macroContextSupport;
    private final AnalysisSentimentContextSupport sentimentContextSupport;
    private final AnalysisOnchainContextSupport onchainContextSupport;
    private final AnalysisReportFormattingSupport formattingSupport;
    private final AnalysisTextLocalizationSupport textLocalizationSupport;

    AnalysisSummarySectionAssembler(
            AnalysisIndicatorStateSupport indicatorStateSupport,
            AnalysisComparisonWindowSupport comparisonWindowSupport,
            AnalysisDerivativeContextSupport derivativeContextSupport,
            AnalysisMacroContextSupport macroContextSupport,
            AnalysisSentimentContextSupport sentimentContextSupport,
            AnalysisOnchainContextSupport onchainContextSupport,
            AnalysisReportFormattingSupport formattingSupport
    ) {
        this.indicatorStateSupport = indicatorStateSupport;
        this.comparisonWindowSupport = comparisonWindowSupport;
        this.derivativeContextSupport = derivativeContextSupport;
        this.macroContextSupport = macroContextSupport;
        this.sentimentContextSupport = sentimentContextSupport;
        this.onchainContextSupport = onchainContextSupport;
        this.formattingSupport = formattingSupport;
        this.textLocalizationSupport = new AnalysisTextLocalizationSupport();
    }

    AnalysisSummaryPayload buildSummary(
            MarketIndicatorSnapshotEntity snapshot,
            AnalysisTrendLabel trendBias,
            AnalysisReportType reportType,
            List<AnalysisComparisonFact> comparisonFacts,
            List<AnalysisComparisonHighlight> comparisonHighlights,
            List<AnalysisWindowSummary> windowSummaries,
            AnalysisDerivativeContext derivativeContext,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            List<AnalysisContinuityNote> continuityNotes,
            AnalysisExternalContextCompositePayload externalContextComposite,
            AnalysisLevelContextPayload levelContext
    ) {
        String headline = reportHeadline(reportType);
        AnalysisContextHeadlinePayload comparisonHeadline = comparisonWindowSupport.comparisonContextHeadline(
                reportType,
                comparisonFacts,
                comparisonHighlights
        );
        AnalysisContextHeadlinePayload windowHeadline = comparisonWindowSupport.windowContextHeadline(reportType, windowSummaries);
        AnalysisContextHeadlinePayload derivativeHeadline = derivativeContextSupport.derivativeContextHeadline(
                reportType,
                derivativeContext
        );
        AnalysisContextHeadlinePayload macroHeadline = macroContextSupport.macroContextHeadline(
                reportType,
                macroContext
        );
        AnalysisContextHeadlinePayload sentimentHeadline = sentimentContextSupport.sentimentContextHeadline(
                reportType,
                sentimentContext
        );
        AnalysisContextHeadlinePayload onchainHeadline = onchainContextSupport.onchainContextHeadline(
                reportType,
                onchainContext
        );
        AnalysisContextHeadlinePayload externalHeadline = externalContextHeadline(externalContextComposite);
        List<AnalysisContextHeadlinePayload> signalHeadlines = java.util.stream.Stream.of(
                        comparisonHeadline,
                        windowHeadline,
                        derivativeHeadline,
                        macroHeadline,
                        sentimentHeadline,
                        onchainHeadline,
                        externalHeadline
                )
                .filter(java.util.Objects::nonNull)
                .map(this::localizeHeadline)
                .toList();

        AnalysisSummaryKeyMessagePayload keyMessage = new AnalysisSummaryKeyMessagePayload(
                snapshot.getSymbol()
                        + "는 현재 "
                        + trendBiasLabel(trendBias)
                        + " 구조로 평가되며, 현재 가격은 "
                        + formattingSupport.plain(snapshot.getCurrentPrice())
                        + ", RSI14는 "
                        + formattingSupport.plain(snapshot.getRsi14())
                        + ", MACD 히스토그램은 "
                        + formattingSupport.plain(snapshot.getMacdHistogram())
                        + "입니다. "
                        + externalContextSentence(reportType, macroContext, sentimentContext, onchainContext, externalContextComposite),
                summarySignalDetails(signalHeadlines, levelContext),
                continuityNotes.isEmpty() ? null : textLocalizationSupport.localizeSentence(continuityNotes.get(0).summary())
        );

        AnalysisOutlookType outlook = switch (trendBias) {
            case BULLISH -> AnalysisOutlookType.CONSTRUCTIVE;
            case BEARISH -> AnalysisOutlookType.DEFENSIVE;
            case NEUTRAL -> AnalysisOutlookType.NEUTRAL;
        };

        return new AnalysisSummaryPayload(
                headline,
                outlook,
                indicatorStateSupport.confidenceLabel(snapshot, comparisonFacts, derivativeContext),
                keyMessage,
                signalHeadlines
        );
    }

    private List<String> summarySignalDetails(
            List<AnalysisContextHeadlinePayload> signalHeadlines,
            AnalysisLevelContextPayload levelContext
    ) {
        List<String> details = new ArrayList<>(signalHeadlines.stream()
                                                              .map(AnalysisContextHeadlinePayload::detail)
                                                              .toList());
        if (levelContext != null && levelContext.nearestSupportZone() != null) {
            details.add("가까운 지지 "
                    + formattingSupport.zoneLabel(
                    levelContext.nearestSupportZone().zoneLow(),
                    levelContext.nearestSupportZone().zoneHigh()
            )
                    + "에 "
                    + levelContext.nearestSupportZone().levelCount()
                    + "개의 후보 레벨이 밀집해 있습니다."
            );
        }
        if (levelContext != null && levelContext.nearestResistanceZone() != null) {
            details.add("가까운 저항 "
                    + formattingSupport.zoneLabel(
                    levelContext.nearestResistanceZone().zoneLow(),
                    levelContext.nearestResistanceZone().zoneHigh()
            )
                    + "에 "
                    + levelContext.nearestResistanceZone().levelCount()
                    + "개의 후보 레벨이 밀집해 있습니다."
            );
        }
        if (levelContext != null) {
            levelContext.zoneInteractionFacts().stream()
                    .map(AnalysisZoneInteractionFact::summary)
                    .forEach(details::add);
            levelContext.highlights().stream()
                    .map(AnalysisLevelContextHighlight::detail)
                    .forEach(details::add);
        }
        if (levelContext != null && levelContext.comparisonFacts() != null) {
            levelContext.comparisonFacts().stream()
                        .limit(2)
                        .map(fact -> fact.reference().name() + " 기준 레벨 맥락 변화가 반영됐습니다.")
                        .forEach(details::add);
        }
        return details;
    }

    private String externalContextSentence(
            AnalysisReportType reportType,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext,
            AnalysisExternalContextCompositePayload externalContextComposite
    ) {
        List<String> clauses = new ArrayList<>();

        if (macroContext != null) {
            var macroWindowSummary = macroContextSupport.primaryWindowSummary(reportType, macroContext);
            if (macroWindowSummary != null) {
                clauses.add("거시 맥락에서 DXY는 평균 대비 "
                        + formattingSupport.signedRatio(macroWindowSummary.currentDxyProxyVsAverage())
                        + " 수준입니다");
            }
        }

        if (sentimentContext != null) {
            var sentimentWindowSummary = sentimentContextSupport.primaryWindowSummary(reportType, sentimentContext);
            if (sentimentWindowSummary != null && sentimentWindowSummary.currentIndexVsAverage() != null) {
                clauses.add("심리 지표는 평균 대비 "
                        + formattingSupport.signedRatio(sentimentWindowSummary.currentIndexVsAverage())
                        + " 수준입니다");
            }
        }

        if (onchainContext != null) {
            var onchainWindowSummary = onchainContextSupport.primaryWindowSummary(reportType, onchainContext);
            if (onchainWindowSummary != null && onchainWindowSummary.currentActiveAddressVsAverage() != null) {
                clauses.add("온체인 활동은 평균 대비 "
                        + formattingSupport.signedRatio(onchainWindowSummary.currentActiveAddressVsAverage())
                        + " 수준입니다");
            }
        }

        if (externalContextComposite != null && externalContextComposite.primarySignalTitle() != null) {
            if (externalContextComposite.transitions() != null && !externalContextComposite.transitions().isEmpty()) {
                clauses.add(localizeSentence(externalContextComposite.transitions().get(0).summary()));
            } else if (externalContextComposite.highlights() != null && !externalContextComposite.highlights().isEmpty()) {
                clauses.add(localizeSentence(externalContextComposite.highlights().get(0).summary()));
            } else {
                clauses.add("주요 외부 국면은 "
                        + localizePhrase(externalContextComposite.primarySignalTitle())
                        + "이며, 종합 리스크 점수는 "
                        + formattingSupport.plain(externalContextComposite.compositeRiskScore())
                        + "입니다");
            }
        }

        if (externalContextComposite != null
                && externalContextComposite.windowSummaries() != null
                && !externalContextComposite.windowSummaries().isEmpty()) {
            var primaryWindowSummary = externalContextComposite.windowSummaries().get(externalContextComposite.windowSummaries().size() - 1);
            if (primaryWindowSummary.currentCompositeRiskVsAverage() != null) {
                clauses.add(externalRiskVsAverageSentence(primaryWindowSummary));
            }
        }
        if (externalContextComposite != null && externalContextComposite.persistence() != null) {
            clauses.add(localizeSentence(externalContextComposite.persistence().summary()));
        }
        if (externalContextComposite != null && externalContextComposite.state() != null) {
            clauses.add("외부 국면 반전 위험은 전체 스케일 대비 "
                    + formattingSupport.signedRatio(externalContextComposite.state().reversalRiskScore())
                    .replace("+", "")
                    + "입니다");
        }

        return clauses.isEmpty() ? "외부 맥락은 아직 혼조적입니다." : String.join(", ", clauses) + ".";
    }

    private AnalysisContextHeadlinePayload externalContextHeadline(
            AnalysisExternalContextCompositePayload externalContextComposite
    ) {
        if (externalContextComposite == null) {
            return null;
        }
        if (externalContextComposite.transitions() != null && !externalContextComposite.transitions().isEmpty()) {
            var transition = externalContextComposite.transitions().get(0);
            return new AnalysisContextHeadlinePayload(
                    com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory.EXTERNAL,
                    transition.transitionType().name(),
                    localizeSentence(transition.summary()),
                    com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineImportance.HIGH
            );
        }
        if (externalContextComposite.highlights() != null && !externalContextComposite.highlights().isEmpty()) {
            var highlight = externalContextComposite.highlights().get(0);
            return new AnalysisContextHeadlinePayload(
                    com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory.EXTERNAL,
                    highlight.title(),
                    localizeSentence(highlight.summary()),
                    highlight.importance()
            );
        }
        if (externalContextComposite.primarySignalTitle() == null) {
            return null;
        }
        return new AnalysisContextHeadlinePayload(
                com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory.EXTERNAL,
                externalContextComposite.primarySignalTitle(),
                localizeSentence(externalContextComposite.primarySignalDetail()),
                com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineImportance.MEDIUM
        );
    }

    private AnalysisContextHeadlinePayload localizeHeadline(AnalysisContextHeadlinePayload headline) {
        return new AnalysisContextHeadlinePayload(
                headline.category(),
                headline.title(),
                localizeSentence(headline.detail()),
                headline.importance()
        );
    }

    private String reportHeadline(AnalysisReportType reportType) {
        if (reportType == null) {
            return "report view";
        }
        return switch (reportType) {
            case SHORT_TERM -> "SHORT_TERM view";
            case MID_TERM -> "MID_TERM view";
            case LONG_TERM -> "LONG_TERM view";
        };
    }

    private String trendBiasLabel(AnalysisTrendLabel trendBias) {
        if (trendBias == null) {
            return "중립";
        }
        return switch (trendBias) {
            case BULLISH -> "상승 우위";
            case BEARISH -> "하락 우위";
            case NEUTRAL -> "중립";
        };
    }

    private String externalRiskVsAverageSentence(com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextWindowSummary summary) {
        BigDecimal ratio = summary.currentCompositeRiskVsAverage();
        String windowLabel = marketWindowLabel(summary.windowType());
        if (ratio == null) {
            return windowLabel + " 외부 종합 리스크는 평균 대비 확인이 필요합니다";
        }
        if (ratio.compareTo(new BigDecimal("1.00")) >= 0) {
            BigDecimal multiple = BigDecimal.ONE.add(ratio);
            return windowLabel + " 외부 종합 리스크는 평균 대비 약 " + formattingSupport.plain(multiple) + "배 높은 수준입니다";
        }
        if (ratio.compareTo(new BigDecimal("0.15")) >= 0) {
            return windowLabel + " 외부 종합 리스크는 평균보다 높은 수준입니다";
        }
        if (ratio.compareTo(new BigDecimal("-0.15")) > 0) {
            return windowLabel + " 외부 종합 리스크는 평균과 비슷한 수준입니다";
        }
        if (ratio.compareTo(new BigDecimal("-0.50")) > 0) {
            return windowLabel + " 외부 종합 리스크는 평균보다 낮은 수준입니다";
        }
        return windowLabel + " 외부 종합 리스크는 평균 대비 크게 낮은 수준입니다";
    }

    private String marketWindowLabel(MarketWindowType windowType) {
        if (windowType == null) {
            return "최근 기준";
        }
        return switch (windowType) {
            case LAST_1D -> "최근 1일 기준";
            case LAST_3D -> "최근 3일 기준";
            case LAST_7D -> "최근 7일 기준";
            case LAST_14D -> "최근 14일 기준";
            case LAST_30D -> "최근 30일 기준";
            case LAST_90D -> "최근 90일 기준";
            case LAST_180D -> "최근 180일 기준";
            case LAST_52W -> "최근 52주 기준";
        };
    }

    private String localizeSentence(String value) {
        return textLocalizationSupport.localizeSentence(value);
    }

    private String localizePhrase(String value) {
        return textLocalizationSupport.localizePhrase(value);
    }
}
