package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisDerivativeHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextCompositePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalContextHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimePersistence;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimeSignal;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimeStatePayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisExternalRegimeTransition;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisOnchainHighlight;
import com.aicoinassist.batch.domain.report.dto.AnalysisRiskFactor;
import com.aicoinassist.batch.domain.report.dto.AnalysisScenario;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentHighlight;

import java.util.List;

class AnalysisReportBodyLocalizationSupport {

    private final AnalysisTextLocalizationSupport textLocalizationSupport = new AnalysisTextLocalizationSupport();

    List<AnalysisContinuityNote> localizeContinuityNotes(List<AnalysisContinuityNote> continuityNotes) {
        if (continuityNotes == null) {
            return List.of();
        }
        return continuityNotes.stream()
                              .map(note -> new AnalysisContinuityNote(
                                      note.reference(),
                                      note.previousAnalysisBasisTime(),
                                      textLocalizationSupport.localizeSentence(note.summary())
                              ))
                              .toList();
    }

    AnalysisDerivativeContext localizeDerivativeContext(AnalysisDerivativeContext context) {
        if (context == null) {
            return null;
        }
        return new AnalysisDerivativeContext(
                context.snapshotTime(),
                context.openInterestSourceEventTime(),
                context.premiumIndexSourceEventTime(),
                context.sourceDataVersion(),
                context.openInterest(),
                context.markPrice(),
                context.indexPrice(),
                context.lastFundingRate(),
                context.nextFundingTime(),
                context.markIndexBasisRate(),
                context.comparisonFacts(),
                context.windowSummaries(),
                context.highlights() == null ? List.of() : context.highlights().stream()
                        .map(this::localizeDerivativeHighlight)
                        .toList()
        );
    }

    AnalysisMacroContext localizeMacroContext(AnalysisMacroContext context) {
        if (context == null) {
            return null;
        }
        return new AnalysisMacroContext(
                context.snapshotTime(),
                context.sourceDataVersion(),
                context.dxyObservationDate(),
                context.us10yYieldObservationDate(),
                context.usdKrwObservationDate(),
                context.dxyProxyValue(),
                context.us10yYieldValue(),
                context.usdKrwValue(),
                context.comparisonFacts(),
                context.windowSummaries(),
                context.highlights() == null ? List.of() : context.highlights().stream()
                        .map(this::localizeMacroHighlight)
                        .toList()
        );
    }

    AnalysisSentimentContext localizeSentimentContext(AnalysisSentimentContext context) {
        if (context == null) {
            return null;
        }
        return new AnalysisSentimentContext(
                context.snapshotTime(),
                context.sourceEventTime(),
                context.sourceDataVersion(),
                context.indexValue(),
                context.classification(),
                context.timeUntilUpdateSeconds(),
                context.comparisonFacts(),
                context.windowSummaries(),
                context.highlights() == null ? List.of() : context.highlights().stream()
                        .map(this::localizeSentimentHighlight)
                        .toList()
        );
    }

    AnalysisOnchainContext localizeOnchainContext(AnalysisOnchainContext context) {
        if (context == null) {
            return null;
        }
        return new AnalysisOnchainContext(
                context.snapshotTime(),
                context.activeAddressSourceEventTime(),
                context.transactionCountSourceEventTime(),
                context.marketCapSourceEventTime(),
                context.sourceDataVersion(),
                context.activeAddressCount(),
                context.transactionCount(),
                context.marketCapUsd(),
                context.comparisonFacts(),
                context.windowSummaries(),
                context.highlights() == null ? List.of() : context.highlights().stream()
                        .map(this::localizeOnchainHighlight)
                        .toList()
        );
    }

    AnalysisExternalContextCompositePayload localizeExternalContextComposite(AnalysisExternalContextCompositePayload payload) {
        if (payload == null) {
            return null;
        }
        return new AnalysisExternalContextCompositePayload(
                payload.snapshotTime(),
                payload.sourceDataVersion(),
                payload.compositeRiskScore(),
                payload.dominantDirection(),
                payload.highestSeverity(),
                payload.supportiveSignalCount(),
                payload.cautionarySignalCount(),
                payload.headwindSignalCount(),
                payload.primarySignalCategory(),
                payload.primarySignalTitle(),
                textLocalizationSupport.localizeSentence(payload.primarySignalDetail()),
                payload.regimeSignals() == null ? List.of() : payload.regimeSignals().stream()
                        .map(this::localizeExternalRegimeSignal)
                        .toList(),
                payload.comparisonFacts(),
                payload.highlights() == null ? List.of() : payload.highlights().stream()
                        .map(this::localizeExternalContextHighlight)
                        .toList(),
                payload.windowSummaries(),
                payload.transitions() == null ? List.of() : payload.transitions().stream()
                        .map(this::localizeExternalRegimeTransition)
                        .toList(),
                localizeExternalRegimePersistence(payload.persistence()),
                localizeExternalRegimeState(payload.state())
        );
    }

    List<AnalysisRiskFactor> localizeRiskFactors(List<AnalysisRiskFactor> riskFactors) {
        if (riskFactors == null) {
            return List.of();
        }
        return riskFactors.stream()
                          .map(riskFactor -> new AnalysisRiskFactor(
                                  riskFactor.type(),
                                  riskFactor.title(),
                                  textLocalizationSupport.localizeSentence(riskFactor.summary()),
                                  localizeSentences(riskFactor.triggerFacts())
                          ))
                          .toList();
    }

    List<AnalysisScenario> localizeScenarios(List<AnalysisScenario> scenarios) {
        if (scenarios == null) {
            return List.of();
        }
        return scenarios.stream()
                        .map(scenario -> new AnalysisScenario(
                                scenario.title(),
                                scenario.bias(),
                                localizeSentences(scenario.triggerConditions()),
                                textLocalizationSupport.localizeSentence(scenario.pathSummary()),
                                localizeSentences(scenario.invalidationSignals())
                        ))
                        .toList();
    }

    private AnalysisDerivativeHighlight localizeDerivativeHighlight(AnalysisDerivativeHighlight highlight) {
        return new AnalysisDerivativeHighlight(
                highlight.title(),
                textLocalizationSupport.localizeSentence(highlight.summary()),
                highlight.importance(),
                highlight.relatedMetric(),
                highlight.reference(),
                highlight.windowType()
        );
    }

    private AnalysisMacroHighlight localizeMacroHighlight(AnalysisMacroHighlight highlight) {
        return new AnalysisMacroHighlight(
                highlight.title(),
                textLocalizationSupport.localizeSentence(highlight.summary()),
                highlight.importance(),
                highlight.reference()
        );
    }

    private AnalysisSentimentHighlight localizeSentimentHighlight(AnalysisSentimentHighlight highlight) {
        return new AnalysisSentimentHighlight(
                highlight.title(),
                textLocalizationSupport.localizeSentence(highlight.summary()),
                highlight.importance(),
                highlight.reference()
        );
    }

    private AnalysisOnchainHighlight localizeOnchainHighlight(AnalysisOnchainHighlight highlight) {
        return new AnalysisOnchainHighlight(
                highlight.title(),
                textLocalizationSupport.localizeSentence(highlight.summary()),
                highlight.importance(),
                highlight.reference()
        );
    }

    private AnalysisExternalRegimeSignal localizeExternalRegimeSignal(AnalysisExternalRegimeSignal signal) {
        return new AnalysisExternalRegimeSignal(
                signal.category(),
                signal.title(),
                textLocalizationSupport.localizeSentence(signal.detail()),
                signal.direction(),
                signal.severity(),
                signal.basisLabel()
        );
    }

    private AnalysisExternalContextHighlight localizeExternalContextHighlight(AnalysisExternalContextHighlight highlight) {
        return new AnalysisExternalContextHighlight(
                highlight.title(),
                textLocalizationSupport.localizeSentence(highlight.summary()),
                highlight.importance(),
                highlight.reference()
        );
    }

    private AnalysisExternalRegimeTransition localizeExternalRegimeTransition(AnalysisExternalRegimeTransition transition) {
        return new AnalysisExternalRegimeTransition(
                transition.reference(),
                transition.referenceTime(),
                transition.transitionType(),
                transition.resultingDirection(),
                transition.resultingSeverity(),
                transition.compositeRiskScoreDelta(),
                textLocalizationSupport.localizeSentence(transition.summary())
        );
    }

    private AnalysisExternalRegimePersistence localizeExternalRegimePersistence(AnalysisExternalRegimePersistence persistence) {
        if (persistence == null) {
            return null;
        }
        return new AnalysisExternalRegimePersistence(
                persistence.windowType(),
                persistence.dominantDirection(),
                persistence.dominantDirectionShare(),
                persistence.highSeverityShare(),
                persistence.persistenceScore(),
                textLocalizationSupport.localizeSentence(persistence.summary())
        );
    }

    private AnalysisExternalRegimeStatePayload localizeExternalRegimeState(AnalysisExternalRegimeStatePayload state) {
        if (state == null) {
            return null;
        }
        return new AnalysisExternalRegimeStatePayload(
                state.dominantDirection(),
                state.highestSeverity(),
                state.primarySignalCategory(),
                state.primarySignalTitle(),
                state.compositeRiskScore(),
                state.reversalRiskScore(),
                textLocalizationSupport.localizeSentence(state.summary())
        );
    }

    private List<String> localizeSentences(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                     .map(textLocalizationSupport::localizeSentence)
                     .toList();
    }
}
