ALTER TABLE analysis_report_shared_context
    DROP INDEX uk_analysis_report_shared_context_identity;

ALTER TABLE analysis_report_shared_context
    MODIFY COLUMN report_type VARCHAR(20) NULL;

ALTER TABLE analysis_report_shared_context
    ADD CONSTRAINT uk_analysis_report_shared_context_identity
        UNIQUE (
            context_version,
            llm_provider,
            llm_model,
            prompt_template_version,
            input_schema_version,
            output_schema_version,
            input_payload_hash
        );

DROP INDEX idx_analysis_report_shared_context_type_basis
    ON analysis_report_shared_context;

CREATE INDEX idx_analysis_report_shared_context_basis
    ON analysis_report_shared_context (analysis_basis_time);
