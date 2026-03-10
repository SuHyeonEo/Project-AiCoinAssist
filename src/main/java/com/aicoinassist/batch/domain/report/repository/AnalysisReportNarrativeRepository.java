package com.aicoinassist.batch.domain.report.repository;

import com.aicoinassist.batch.domain.report.entity.AnalysisReportNarrativeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisReportNarrativeRepository extends JpaRepository<AnalysisReportNarrativeEntity, Long> {

    Optional<AnalysisReportNarrativeEntity> findTopByAnalysisReportIdAndLlmProviderAndLlmModelAndPromptTemplateVersionAndInputSchemaVersionAndOutputSchemaVersionAndInputPayloadHashOrderByIdDesc(
            Long analysisReportId,
            String llmProvider,
            String llmModel,
            String promptTemplateVersion,
            String inputSchemaVersion,
            String outputSchemaVersion,
            String inputPayloadHash
    );
}
