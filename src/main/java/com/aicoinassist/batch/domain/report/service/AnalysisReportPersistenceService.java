package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisReportPayload;
import com.aicoinassist.batch.domain.report.dto.AnalysisReportDraft;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisReportPersistenceService {

    private final AnalysisReportRepository analysisReportRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public AnalysisReportEntity save(AnalysisReportDraft draft) {
        String serializedPayload = serializePayload(draft.reportPayload());

        AnalysisReportEntity existingEntity = analysisReportRepository
                .findTopBySymbolAndReportTypeAndAnalysisBasisTimeAndSourceDataVersionAndAnalysisEngineVersionOrderByIdDesc(
                        draft.symbol(),
                        draft.reportType(),
                        draft.analysisBasisTime(),
                        draft.sourceDataVersion(),
                        draft.analysisEngineVersion()
                )
                .orElse(null);

        if (existingEntity == null) {
            AnalysisReportEntity entity = AnalysisReportEntity.builder()
                                                              .symbol(draft.symbol())
                                                              .reportType(draft.reportType())
                                                              .analysisBasisTime(draft.analysisBasisTime())
                                                              .rawReferenceTime(draft.rawReferenceTime())
                                                              .sourceDataVersion(draft.sourceDataVersion())
                                                              .analysisEngineVersion(draft.analysisEngineVersion())
                                                              .reportPayload(serializedPayload)
                                                              .storedTime(draft.storedTime())
                                                              .build();

            return analysisReportRepository.save(entity);
        }

        existingEntity.refresh(
                draft.rawReferenceTime(),
                serializedPayload,
                draft.storedTime()
        );

        return existingEntity;
    }

    private String serializePayload(AnalysisReportPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize analysis report payload.", exception);
        }
    }
}
