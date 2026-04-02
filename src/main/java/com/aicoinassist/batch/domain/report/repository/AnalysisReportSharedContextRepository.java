package com.aicoinassist.batch.domain.report.repository;

import com.aicoinassist.batch.domain.report.entity.AnalysisReportSharedContextEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisReportSharedContextRepository extends JpaRepository<AnalysisReportSharedContextEntity, Long> {

    Optional<AnalysisReportSharedContextEntity> findTopByContextVersionAndLlmProviderAndLlmModelAndPromptTemplateVersionAndInputSchemaVersionAndOutputSchemaVersionAndInputPayloadHashOrderByIdDesc(
            String contextVersion,
            String llmProvider,
            String llmModel,
            String promptTemplateVersion,
            String inputSchemaVersion,
            String outputSchemaVersion,
            String inputPayloadHash
    );
}
