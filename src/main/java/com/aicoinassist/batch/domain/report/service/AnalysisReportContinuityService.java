package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisContinuityNote;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisComparisonReference;
import com.aicoinassist.batch.domain.report.enumtype.AnalysisReportType;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisReportContinuityService {

    private final AnalysisReportRepository analysisReportRepository;
    private final ObjectMapper objectMapper;

    public List<AnalysisContinuityNote> buildNotes(
            String symbol,
            AnalysisReportType reportType,
            java.time.Instant analysisBasisTime
    ) {
        return analysisReportRepository
                .findTopBySymbolAndReportTypeAndAnalysisBasisTimeLessThanOrderByAnalysisBasisTimeDescIdDesc(
                        symbol,
                        reportType,
                        analysisBasisTime
                )
                .map(previousReport -> List.of(toContinuityNote(reportType, previousReport)))
                .orElseGet(List::of);
    }

    private AnalysisContinuityNote toContinuityNote(
            AnalysisReportType reportType,
            AnalysisReportEntity previousReport
    ) {
        return new AnalysisContinuityNote(
                referenceFor(reportType),
                previousReport.getAnalysisBasisTime(),
                previousSummary(previousReport)
        );
    }

    private AnalysisComparisonReference referenceFor(AnalysisReportType reportType) {
        return switch (reportType) {
            case SHORT_TERM -> AnalysisComparisonReference.PREV_SHORT_REPORT;
            case MID_TERM -> AnalysisComparisonReference.PREV_MID_REPORT;
            case LONG_TERM -> AnalysisComparisonReference.PREV_LONG_REPORT;
        };
    }

    private String previousSummary(AnalysisReportEntity previousReport) {
        try {
            JsonNode root = objectMapper.readTree(previousReport.getReportPayload());
            JsonNode summaryNode = root.get("summary");
            if (summaryNode == null || summaryNode.isNull()) {
                return "Previous report exists but summary is unavailable.";
            }
            if (summaryNode.isTextual()) {
                return summaryNode.asText();
            }

            JsonNode keyMessageNode = summaryNode.get("keyMessage");
            if (keyMessageNode != null && !keyMessageNode.isNull()) {
                if (keyMessageNode.isTextual() && !keyMessageNode.asText().isBlank()) {
                    return keyMessageNode.asText();
                }

                JsonNode primaryMessageNode = keyMessageNode.get("primaryMessage");
                if (primaryMessageNode != null && !primaryMessageNode.isNull() && !primaryMessageNode.asText().isBlank()) {
                    return primaryMessageNode.asText();
                }
            }

            JsonNode headlineNode = summaryNode.get("headline");
            if (headlineNode != null && !headlineNode.isNull() && !headlineNode.asText().isBlank()) {
                return headlineNode.asText();
            }

            return "Previous report exists but summary is unavailable.";
        } catch (Exception exception) {
            return "Previous report exists but summary could not be parsed.";
        }
    }
}
