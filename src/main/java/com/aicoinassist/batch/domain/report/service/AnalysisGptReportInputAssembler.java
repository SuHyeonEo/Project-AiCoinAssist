package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisGptReportInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AnalysisGptReportInputAssembler {

    private final AnalysisGptCrossSignalFactory analysisGptCrossSignalFactory;

    public AnalysisGptReportInputPayload assemble(
            AnalysisReportEntity reportEntity,
            AnalysisReportPayload reportPayload
    ) {
        return new AnalysisGptReportInputPayload(
                reportEntity.getSymbol(),
                reportEntity.getReportType(),
                reportEntity.getAnalysisBasisTime(),
                reportEntity.getRawReferenceTime(),
                reportEntity.getSourceDataVersion(),
                reportEntity.getAnalysisEngineVersion(),
                reportPayload.summary(),
                reportPayload.marketContext() == null ? null : reportPayload.marketContext().currentState(),
                reportPayload.marketContext() == null ? null : reportPayload.marketContext().comparisonContext(),
                reportPayload.marketContext() == null ? null : reportPayload.marketContext().windowContext(),
                reportPayload.marketContext() == null ? null : reportPayload.marketContext().derivativeContextSummary(),
                reportPayload.marketContext() == null ? null : reportPayload.marketContext().macroContextSummary(),
                reportPayload.marketContext() == null ? null : reportPayload.marketContext().sentimentContextSummary(),
                reportPayload.marketContext() == null ? null : reportPayload.marketContext().onchainContextSummary(),
                reportPayload.marketContext() == null ? null : reportPayload.marketContext().levelContext(),
                reportPayload.marketContext() == null ? null : reportPayload.marketContext().externalContextComposite(),
                reportPayload.summary() == null ? List.of() : reportPayload.summary().signalHeadlines(),
                primaryFacts(reportPayload),
                analysisGptCrossSignalFactory.build(reportPayload),
                reportPayload.riskFactors(),
                reportPayload.scenarios(),
                reportPayload.continuityNotes()
        );
    }

    private List<String> primaryFacts(AnalysisReportPayload payload) {
        LinkedHashSet<String> facts = new LinkedHashSet<>();
        if (payload.summary() != null && payload.summary().keyMessage() != null) {
            addIfPresent(facts, payload.summary().keyMessage().primaryMessage());
            payload.summary().keyMessage().signalDetails().stream().limit(4).forEach(detail -> addIfPresent(facts, detail));
            addIfPresent(facts, payload.summary().keyMessage().continuityMessage());
        }
        if (payload.marketContext() != null) {
            if (payload.marketContext().comparisonContext() != null && payload.marketContext().comparisonContext().factSummary() != null) {
                addIfPresent(facts, payload.marketContext().comparisonContext().factSummary().primaryFact());
                payload.marketContext().comparisonContext().factSummary().referenceBreakdown().stream()
                        .limit(2)
                        .forEach(detail -> addIfPresent(facts, detail));
            }
            if (payload.marketContext().windowContext() != null && payload.marketContext().windowContext().summary() != null) {
                addIfPresent(facts, payload.marketContext().windowContext().summary().rangeSummary());
                addIfPresent(facts, payload.marketContext().windowContext().summary().rangePositionSummary());
            }
            addIfPresent(facts, payload.marketContext().derivativeContextSummary() == null ? null : payload.marketContext().derivativeContextSummary().currentStateSummary());
            addIfPresent(facts, payload.marketContext().macroContextSummary() == null ? null : payload.marketContext().macroContextSummary().currentStateSummary());
            addIfPresent(facts, payload.marketContext().sentimentContextSummary() == null ? null : payload.marketContext().sentimentContextSummary().currentStateSummary());
            addIfPresent(facts, payload.marketContext().onchainContextSummary() == null ? null : payload.marketContext().onchainContextSummary().currentStateSummary());
            if (payload.marketContext().levelContext() != null) {
                payload.marketContext().levelContext().zoneInteractionFacts().stream()
                        .limit(2)
                        .forEach(fact -> addIfPresent(facts, fact.summary()));
            }
            if (payload.marketContext().externalContextComposite() != null) {
                if (payload.marketContext().externalContextComposite().state() != null) {
                    addIfPresent(facts, payload.marketContext().externalContextComposite().state().summary());
                } else {
                    addIfPresent(facts, payload.marketContext().externalContextComposite().primarySignalDetail());
                }
            }
        }
        return new ArrayList<>(facts).stream().limit(12).toList();
    }

    private void addIfPresent(LinkedHashSet<String> facts, String value) {
        if (value != null && !value.isBlank()) {
            facts.add(value);
        }
    }
}
