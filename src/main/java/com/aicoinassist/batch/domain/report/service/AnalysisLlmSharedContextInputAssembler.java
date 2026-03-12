package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisGptReportInputPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisMacroContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisSentimentContext;
import com.aicoinassist.batch.domain.report.dto.AnalysisLlmSharedContextInputPayload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
                sharedContextVersion(input),
                input.analysisEngineVersion(),
                macroFacts(input),
                sentimentFacts(input)
        );
    }

    private List<String> macroFacts(AnalysisGptReportInputPayload input) {
        LinkedHashSet<String> facts = new LinkedHashSet<>();
        AnalysisMacroContext context = input.macroFactContext();
        if (context != null) {
            addIfPresent(facts, macroValueFact(context));
            addIfPresent(facts, macroObservationFact(context));
        }
        return new ArrayList<>(facts).stream().limit(6).toList();
    }

    private List<String> sentimentFacts(AnalysisGptReportInputPayload input) {
        LinkedHashSet<String> facts = new LinkedHashSet<>();
        AnalysisSentimentContext context = input.sentimentFactContext();
        if (context != null) {
            addIfPresent(facts, sentimentValueFact(context));
        }
        return new ArrayList<>(facts).stream().limit(6).toList();
    }

    private String sharedContextVersion(AnalysisGptReportInputPayload input) {
        String basis = "macro=" + (input.macroFactContext() == null ? "none" : input.macroFactContext().sourceDataVersion())
                + "|sentiment=" + (input.sentimentFactContext() == null ? "none" : input.sentimentFactContext().sourceDataVersion());
        return sha256(basis).substring(0, 16);
    }

    private String macroValueFact(AnalysisMacroContext context) {
        if (context.dxyProxyValue() == null && context.us10yYieldValue() == null && context.usdKrwValue() == null) {
            return null;
        }
        return "거시 현재값은 DXY "
                + decimal(context.dxyProxyValue())
                + ", US10Y "
                + decimal(context.us10yYieldValue())
                + ", USD/KRW "
                + decimal(context.usdKrwValue())
                + "입니다.";
    }

    private String macroObservationFact(AnalysisMacroContext context) {
        if (context.dxyObservationDate() == null && context.us10yYieldObservationDate() == null && context.usdKrwObservationDate() == null) {
            return null;
        }
        return "거시 관측일은 DXY "
                + safeDate(context.dxyObservationDate() == null ? null : context.dxyObservationDate().toString())
                + ", US10Y "
                + safeDate(context.us10yYieldObservationDate() == null ? null : context.us10yYieldObservationDate().toString())
                + ", USD/KRW "
                + safeDate(context.usdKrwObservationDate() == null ? null : context.usdKrwObservationDate().toString())
                + "입니다.";
    }

    private String sentimentValueFact(AnalysisSentimentContext context) {
        if (context.indexValue() == null && context.classification() == null) {
            return null;
        }
        return "Fear & Greed 지수는 "
                + decimal(context.indexValue())
                + "이며 분류는 "
                + (context.classification() == null ? "확인 필요" : context.classification())
                + "입니다.";
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

    private String decimal(BigDecimal value) {
        return value == null ? "확인 필요" : value.stripTrailingZeros().toPlainString();
    }

    private String safeDate(String value) {
        return value == null || value.isBlank() ? "확인 필요" : value;
    }

}
