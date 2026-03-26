package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.enumtype.MarketWindowType;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonFact;
import com.aicoinassist.batch.domain.report.dto.AnalysisComparisonHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisContextHeadlinePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisWindowSummary;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineCategory;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisContextHeadlineImportance;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class AnalysisComparisonWindowSupport {

    private final AnalysisReportFormattingSupport formattingSupport;
    private final AnalysisTextLocalizationSupport textLocalizationSupport;

    AnalysisComparisonWindowSupport(AnalysisReportFormattingSupport formattingSupport) {
        this.formattingSupport = formattingSupport;
        this.textLocalizationSupport = new AnalysisTextLocalizationSupport();
    }

    List<AnalysisComparisonHighlight> comparisonHighlights(
            AnalysisReportType reportType,
            List<AnalysisComparisonFact> comparisonFacts
    ) {
        Map<AnalysisComparisonReference, AnalysisComparisonFact> factByReference = comparisonFacts.stream()
                                                                                                  .collect(Collectors.toMap(
                                                                                                          AnalysisComparisonFact::reference,
                                                                                                          fact -> fact,
                                                                                                          (left, right) -> left
                                                                                                  ));

        return highlightPriority(reportType).stream()
                                            .map(factByReference::get)
                                            .filter(java.util.Objects::nonNull)
                                            .map(this::toHighlight)
                                            .toList();
    }

    List<AnalysisWindowHighlight> windowHighlights(
            AnalysisReportType reportType,
            List<AnalysisWindowSummary> windowSummaries
    ) {
        Map<MarketWindowType, AnalysisWindowSummary> summaryByType = windowSummaries.stream()
                                                                                    .collect(Collectors.toMap(
                                                                                            AnalysisWindowSummary::windowType,
                                                                                            summary -> summary,
                                                                                            (left, right) -> left
                                                                                    ));

        return (switch (reportType) {
            case SHORT_TERM -> List.of(
                    summaryByType.get(MarketWindowType.LAST_1D),
                    summaryByType.get(MarketWindowType.LAST_7D)
            );
            case MID_TERM -> List.of(
                    summaryByType.get(MarketWindowType.LAST_7D),
                    summaryByType.get(MarketWindowType.LAST_30D)
            );
            case LONG_TERM -> List.of(
                    summaryByType.get(MarketWindowType.LAST_180D),
                    summaryByType.get(MarketWindowType.LAST_52W)
            );
        }).stream()
         .filter(java.util.Objects::nonNull)
         .map(this::toWindowHighlight)
         .toList();
    }

    AnalysisContextHeadlinePayload comparisonContextHeadline(
            AnalysisReportType reportType,
            List<AnalysisComparisonFact> comparisonFacts,
            List<AnalysisComparisonHighlight> comparisonHighlights
    ) {
        if (!comparisonHighlights.isEmpty()) {
            AnalysisComparisonHighlight highlight = comparisonHighlights.get(0);
            return new AnalysisContextHeadlinePayload(
                    AnalysisContextHeadlineCategory.COMPARISON,
                    highlight.reference().name() + " comparison",
                    highlight.detail(),
                    reportType == AnalysisReportType.SHORT_TERM
                            ? AnalysisContextHeadlineImportance.HIGH
                            : AnalysisContextHeadlineImportance.MEDIUM
            );
        }
        if (!comparisonFacts.isEmpty()) {
            AnalysisComparisonFact fact = comparisonFacts.get(0);
            return new AnalysisContextHeadlinePayload(
                    AnalysisContextHeadlineCategory.COMPARISON,
                    fact.reference().name() + " comparison",
                    comparisonFactSummary(fact),
                    AnalysisContextHeadlineImportance.MEDIUM
            );
        }
        return null;
    }

    AnalysisContextHeadlinePayload windowContextHeadline(
            AnalysisReportType reportType,
            List<AnalysisWindowSummary> windowSummaries
    ) {
        AnalysisWindowSummary primaryWindow = primaryWindow(windowSummaries);
        if (primaryWindow == null) {
            return null;
        }
        return new AnalysisContextHeadlinePayload(
                AnalysisContextHeadlineCategory.WINDOW,
                primaryWindow.windowType().name() + " position",
                textLocalizationSupport.windowLabel(primaryWindow.windowType())
                        + " 기준 가격은 레인지의 "
                        + formattingSupport.percentage(primaryWindow.currentPositionInRange())
                        + " 위치이며 "
                        + windowParticipationSummary(primaryWindow),
                reportType == AnalysisReportType.LONG_TERM
                        ? AnalysisContextHeadlineImportance.HIGH
                        : AnalysisContextHeadlineImportance.MEDIUM
        );
    }

    AnalysisWindowSummary primaryWindow(List<AnalysisWindowSummary> windowSummaries) {
        if (windowSummaries == null || windowSummaries.isEmpty()) {
            return null;
        }
        return windowSummaries.get(windowSummaries.size() - 1);
    }

    String comparisonFactSummary(AnalysisComparisonFact fact) {
        return fact.reference().name()
                + " 기준 가격은 "
                + formattingSupport.signed(fact.priceChangeRate())
                + "%, RSI 변화는 "
                + formattingSupport.signed(fact.rsiDelta())
                + ", MACD 히스토그램 변화는 "
                + formattingSupport.signed(fact.macdHistogramDelta());
    }

    private List<AnalysisComparisonReference> highlightPriority(AnalysisReportType reportType) {
        return switch (reportType) {
            case SHORT_TERM -> List.of(
                    AnalysisComparisonReference.PREV_BATCH,
                    AnalysisComparisonReference.D1,
                    AnalysisComparisonReference.D3
            );
            case MID_TERM -> List.of(
                    AnalysisComparisonReference.D7,
                    AnalysisComparisonReference.D14,
                    AnalysisComparisonReference.D30
            );
            case LONG_TERM -> List.of(
                    AnalysisComparisonReference.Y52_HIGH,
                    AnalysisComparisonReference.Y52_LOW,
                    AnalysisComparisonReference.D180
            );
        };
    }

    private AnalysisComparisonHighlight toHighlight(AnalysisComparisonFact fact) {
        return switch (fact.reference()) {
            case PREV_BATCH -> new AnalysisComparisonHighlight(
                    fact.reference(),
                    "직전 배치 대비 가격은 " + formattingSupport.signed(fact.priceChangeRate()) + "% 움직였고 RSI14는 " + formattingSupport.signed(fact.rsiDelta()) + " 변했습니다.",
                    "PREV_BATCH 기준 최신 탄력은 MACD 히스토그램 변화 " + formattingSupport.signed(fact.macdHistogramDelta()) + "로 확인됩니다."
            );
            case D1, D3, D7, D14, D30, D90, D180 -> new AnalysisComparisonHighlight(
                    fact.reference(),
                    fact.reference().name() + " 기준 가격은 비교 시점 대비 " + formattingSupport.signed(fact.priceChangeRate()) + "%입니다.",
                    fact.reference().name() + " 기준 RSI 변화는 " + formattingSupport.signed(fact.rsiDelta()) + "이고 MACD 히스토그램 변화는 " + formattingSupport.signed(fact.macdHistogramDelta()) + "입니다."
            );
            case PREV_SHORT_REPORT, PREV_MID_REPORT, PREV_LONG_REPORT -> new AnalysisComparisonHighlight(
                    fact.reference(),
                    fact.reference().name() + " 대비 가격 변화는 " + formattingSupport.signed(fact.priceChangeRate()) + "%입니다.",
                    fact.reference().name() + " 비교에서 RSI 변화는 " + formattingSupport.signed(fact.rsiDelta()) + "이고 ATR 변화는 " + formattingSupport.signed(fact.atrChangeRate()) + "%입니다."
            );
            case Y52_HIGH -> new AnalysisComparisonHighlight(
                    fact.reference(),
                    "가격은 52주 고점 대비 " + formattingSupport.distanceFromExtremum(fact.priceChangeRate(), "below") + "입니다.",
                    "Y52_HIGH 기준 장기 상단 여력은 사이클 고점 대비 " + formattingSupport.signed(fact.priceChangeRate()) + "%입니다."
            );
            case Y52_LOW -> new AnalysisComparisonHighlight(
                    fact.reference(),
                    "가격은 52주 저점 대비 " + formattingSupport.distanceFromExtremum(fact.priceChangeRate(), "above") + "입니다.",
                    "Y52_LOW 기준 시장은 사이클 저점 대비 " + formattingSupport.signed(fact.priceChangeRate()) + "% 높은 위치입니다."
            );
        };
    }

    private AnalysisWindowHighlight toWindowHighlight(AnalysisWindowSummary summary) {
        return new AnalysisWindowHighlight(
                summary.windowType(),
                textLocalizationSupport.windowLabel(summary.windowType()) + " 기준 가격은 레인지의 " + formattingSupport.percentage(summary.currentPositionInRange()) + " 위치입니다.",
                textLocalizationSupport.windowLabel(summary.windowType())
                        + " 기준 "
                        + windowParticipationSummary(summary)
                        + ", 레인지 고점 대비 거리는 "
                        + formattingSupport.percentage(summary.distanceFromWindowHigh())
                        + "."
        );
    }

    private String windowParticipationSummary(AnalysisWindowSummary summary) {
        List<String> facts = new ArrayList<>();
        facts.add("거래량은 평균 대비 " + formattingSupport.signedRatio(summary.currentVolumeVsAverage()) + "입니다");
        if (summary.currentQuoteAssetVolumeVsAverage() != null) {
            facts.add("거래대금은 평균 대비 " + formattingSupport.signedRatio(summary.currentQuoteAssetVolumeVsAverage()) + "입니다");
        }
        if (summary.currentTradeCountVsAverage() != null) {
            facts.add("체결 수는 평균 대비 " + formattingSupport.signedRatio(summary.currentTradeCountVsAverage()) + "입니다");
        }
        if (summary.currentTakerBuyQuoteRatio() != null) {
            facts.add("시장가 매수 비중은 " + formattingSupport.percentage(summary.currentTakerBuyQuoteRatio()) + "입니다");
        }
        facts.add("ATR은 평균 대비 " + formattingSupport.signedRatio(summary.currentAtrVsAverage()) + "입니다");
        return String.join(", ", facts);
    }
}
