package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.market.dto.MarketExternalContextSnapshot;
import com.aicoinassist.batch.domain.market.dto.MarketExternalRegimeSignalSnapshot;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimeSignal;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeDirection;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisExternalRegimeSeverity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class AnalysisExternalContextSnapshotService {

    private final AnalysisDerivativeContextSupport derivativeContextSupport;
    private final AnalysisMacroContextSupport macroContextSupport;
    private final AnalysisSentimentContextSupport sentimentContextSupport;
    private final AnalysisOnchainContextSupport onchainContextSupport;

    public AnalysisExternalContextSnapshotService() {
        AnalysisReportFormattingSupport formattingSupport = new AnalysisReportFormattingSupport();
        this.derivativeContextSupport = new AnalysisDerivativeContextSupport(formattingSupport);
        this.macroContextSupport = new AnalysisMacroContextSupport(formattingSupport);
        this.sentimentContextSupport = new AnalysisSentimentContextSupport(formattingSupport);
        this.onchainContextSupport = new AnalysisOnchainContextSupport(formattingSupport);
    }

    public MarketExternalContextSnapshot create(
            String symbol,
            AnalysisReportType reportType,
            AnalysisDerivativeContext derivativeContext,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext
    ) {
        List<AnalysisExternalRegimeSignal> regimeSignals = new ArrayList<>();
        if (derivativeContext != null) {
            regimeSignals.addAll(derivativeContextSupport.regimeSignals(reportType, derivativeContext));
        }
        if (macroContext != null) {
            regimeSignals.addAll(macroContextSupport.regimeSignals(reportType, macroContext));
        }
        if (sentimentContext != null) {
            regimeSignals.addAll(sentimentContextSupport.regimeSignals(reportType, sentimentContext));
        }
        if (onchainContext != null) {
            regimeSignals.addAll(onchainContextSupport.regimeSignals(reportType, onchainContext));
        }

        List<AnalysisExternalRegimeSignal> sortedSignals = regimeSignals.stream()
                                                                        .sorted(signalComparator())
                                                                        .toList();
        AnalysisExternalRegimeSignal primarySignal = sortedSignals.isEmpty() ? null : sortedSignals.get(0);

        int supportiveSignalCount = (int) sortedSignals.stream()
                                                       .filter(signal -> signal.direction() == AnalysisExternalRegimeDirection.SUPPORTIVE)
                                                       .count();
        int cautionarySignalCount = (int) sortedSignals.stream()
                                                       .filter(signal -> signal.direction() == AnalysisExternalRegimeDirection.CAUTIONARY)
                                                       .count();
        int headwindSignalCount = (int) sortedSignals.stream()
                                                     .filter(signal -> signal.direction() == AnalysisExternalRegimeDirection.HEADWIND)
                                                     .count();

        BigDecimal supportiveWeight = scoreByDirection(sortedSignals, AnalysisExternalRegimeDirection.SUPPORTIVE);
        BigDecimal cautionaryWeight = scoreByDirection(sortedSignals, AnalysisExternalRegimeDirection.CAUTIONARY);
        BigDecimal headwindWeight = scoreByDirection(sortedSignals, AnalysisExternalRegimeDirection.HEADWIND);
        int signalCount = Math.max(1, sortedSignals.size());
        BigDecimal compositeRiskScore = cautionaryWeight
                .add(headwindWeight)
                .subtract(supportiveWeight)
                .divide(BigDecimal.valueOf(signalCount), 8, RoundingMode.HALF_UP);

        AnalysisExternalRegimeDirection dominantDirection = dominantDirection(
                supportiveWeight,
                cautionaryWeight,
                headwindWeight
        );
        AnalysisExternalRegimeSeverity highestSeverity = sortedSignals.stream()
                                                                      .map(AnalysisExternalRegimeSignal::severity)
                                                                      .filter(Objects::nonNull)
                                                                      .max(Comparator.comparingInt(this::severityWeight))
                                                                      .orElse(null);

        return new MarketExternalContextSnapshot(
                symbol,
                maxInstant(
                        derivativeContext == null ? null : derivativeContext.snapshotTime(),
                        macroContext == null ? null : macroContext.snapshotTime(),
                        sentimentContext == null ? null : sentimentContext.snapshotTime(),
                        onchainContext == null ? null : onchainContext.snapshotTime()
                ),
                derivativeContext == null ? null : derivativeContext.snapshotTime(),
                macroContext == null ? null : macroContext.snapshotTime(),
                sentimentContext == null ? null : sentimentContext.snapshotTime(),
                onchainContext == null ? null : onchainContext.snapshotTime(),
                sourceDataVersion(derivativeContext, macroContext, sentimentContext, onchainContext),
                compositeRiskScore,
                dominantDirection,
                highestSeverity,
                supportiveSignalCount,
                cautionarySignalCount,
                headwindSignalCount,
                primarySignal == null ? null : primarySignal.category(),
                primarySignal == null ? null : primarySignal.title(),
                primarySignal == null ? null : primarySignal.detail(),
                sortedSignals.stream()
                             .map(signal -> new MarketExternalRegimeSignalSnapshot(
                                     signal.category(),
                                     signal.title(),
                                     signal.detail(),
                                     signal.direction(),
                                     signal.severity(),
                                     signal.basisLabel()
                             ))
                             .toList()
        );
    }

    private Comparator<AnalysisExternalRegimeSignal> signalComparator() {
        return Comparator.comparingInt((AnalysisExternalRegimeSignal signal) -> severityWeight(signal.severity())).reversed()
                         .thenComparingInt(signal -> directionWeight(signal.direction())).reversed()
                         .thenComparing(AnalysisExternalRegimeSignal::title, Comparator.nullsLast(String::compareTo));
    }

    private BigDecimal scoreByDirection(
            List<AnalysisExternalRegimeSignal> signals,
            AnalysisExternalRegimeDirection direction
    ) {
        return signals.stream()
                      .filter(signal -> signal.direction() == direction)
                      .map(signal -> BigDecimal.valueOf(severityWeight(signal.severity())))
                      .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private AnalysisExternalRegimeDirection dominantDirection(
            BigDecimal supportiveWeight,
            BigDecimal cautionaryWeight,
            BigDecimal headwindWeight
    ) {
        if (headwindWeight.compareTo(cautionaryWeight) >= 0 && headwindWeight.compareTo(supportiveWeight) > 0) {
            return AnalysisExternalRegimeDirection.HEADWIND;
        }
        if (cautionaryWeight.compareTo(supportiveWeight) > 0) {
            return AnalysisExternalRegimeDirection.CAUTIONARY;
        }
        if (supportiveWeight.compareTo(BigDecimal.ZERO) > 0) {
            return AnalysisExternalRegimeDirection.SUPPORTIVE;
        }
        return null;
    }

    private int directionWeight(AnalysisExternalRegimeDirection direction) {
        if (direction == null) {
            return 0;
        }
        return switch (direction) {
            case HEADWIND -> 3;
            case CAUTIONARY -> 2;
            case SUPPORTIVE -> 1;
        };
    }

    private int severityWeight(AnalysisExternalRegimeSeverity severity) {
        if (severity == null) {
            return 0;
        }
        return switch (severity) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
        };
    }

    private Instant maxInstant(Instant... instants) {
        return Stream.of(instants)
                     .filter(Objects::nonNull)
                     .max(Comparator.naturalOrder())
                     .orElseThrow(() -> new IllegalStateException("No external context snapshot time available."));
    }

    private String sourceDataVersion(
            AnalysisDerivativeContext derivativeContext,
            AnalysisMacroContext macroContext,
            AnalysisSentimentContext sentimentContext,
            AnalysisOnchainContext onchainContext
    ) {
        List<String> parts = new ArrayList<>();
        if (derivativeContext != null) {
            parts.add("derivative=" + derivativeContext.sourceDataVersion());
        }
        if (macroContext != null) {
            parts.add("macro=" + macroContext.sourceDataVersion());
        }
        if (sentimentContext != null) {
            parts.add("sentiment=" + sentimentContext.sourceDataVersion());
        }
        if (onchainContext != null) {
            parts.add("onchain=" + onchainContext.sourceDataVersion());
        }
        return String.join(";", parts);
    }
}
