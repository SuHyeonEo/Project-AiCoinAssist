package com.aicoinassist.batch.domain.report.service;

import com.aicoinassist.batch.domain.report.dto.AnalysisReportDraft;
import com.aicoinassist.batch.domain.report.entity.AnalysisReportEntity;
import com.aicoinassist.batch.domain.report.repository.AnalysisReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisReportPersistenceService {

    private final AnalysisReportRepository analysisReportRepository;

    @Transactional
    public AnalysisReportEntity save(AnalysisReportDraft draft) {
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
                                                              .reportPayload(draft.reportPayload())
                                                              .storedTime(draft.storedTime())
                                                              .build();

            return analysisReportRepository.save(entity);
        }

        existingEntity.refresh(
                draft.rawReferenceTime(),
                draft.reportPayload(),
                draft.storedTime()
        );

        return existingEntity;
    }
}
