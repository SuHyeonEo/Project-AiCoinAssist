package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisGptReportInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextInputPayload;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;

@Component
public class AnalysisLlmSharedContextInputAssembler {

    public AnalysisLlmSharedContextInputPayload assemble(AnalysisGptReportInputPayload input) {
        return new AnalysisLlmSharedContextInputPayload(
                input.reportType(),
                input.analysisBasisTime(),
                input.rawReferenceTime(),
                sharedContextVersion(input),
                input.analysisEngineVersion(),
                macroFacts(input),
                sentimentFacts(input)
        );
    }

    private List<String> macroFacts(AnalysisGptReportInputPayload input) {
        LinkedHashSet<String> facts = new LinkedHashSet<>();
        if (input.macroContext() != null) {
            addIfPresent(facts, input.macroContext().currentStateSummary());
            addIfPresent(facts, input.macroContext().comparisonSummary());
            addIfPresent(facts, input.macroContext().windowSummary());
            if (input.macroContext().highlightDetails() != null) {
                input.macroContext().highlightDetails().stream().limit(3).forEach(value -> addIfPresent(facts, value));
            }
        }
        if (input.macroFactContext() != null && input.macroFactContext().highlights() != null) {
            input.macroFactContext().highlights().stream()
                    .map(highlight -> highlight.summary())
                    .limit(2)
                    .forEach(value -> addIfPresent(facts, value));
        }
        return new ArrayList<>(facts).stream().limit(6).toList();
    }

    private List<String> sentimentFacts(AnalysisGptReportInputPayload input) {
        LinkedHashSet<String> facts = new LinkedHashSet<>();
        if (input.sentimentContext() != null) {
            addIfPresent(facts, input.sentimentContext().currentStateSummary());
            addIfPresent(facts, input.sentimentContext().comparisonSummary());
            addIfPresent(facts, input.sentimentContext().windowSummary());
            if (input.sentimentContext().highlightDetails() != null) {
                input.sentimentContext().highlightDetails().stream().limit(3).forEach(value -> addIfPresent(facts, value));
            }
        }
        if (input.sentimentFactContext() != null && input.sentimentFactContext().highlights() != null) {
            input.sentimentFactContext().highlights().stream()
                    .map(highlight -> highlight.summary())
                    .limit(2)
                    .forEach(value -> addIfPresent(facts, value));
        }
        return new ArrayList<>(facts).stream().limit(6).toList();
    }

    private String sharedContextVersion(AnalysisGptReportInputPayload input) {
        String basis = input.reportType()
                + "|macro=" + (input.macroFactContext() == null ? "none" : input.macroFactContext().sourceDataVersion())
                + "|sentiment=" + (input.sentimentFactContext() == null ? "none" : input.sentimentFactContext().sourceDataVersion());
        return sha256(basis).substring(0, 16);
    }

    private void addIfPresent(LinkedHashSet<String> facts, String value) {
        if (value != null && !value.isBlank()) {
            facts.add(value);
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", exception);
        }
    }
}
