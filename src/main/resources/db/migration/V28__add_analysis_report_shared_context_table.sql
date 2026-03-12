CREATE TABLE analysis_report_shared_context (
    id BIGINT NOT NULL AUTO_INCREMENT,
    report_type VARCHAR(20) NOT NULL,
    analysis_basis_time TIMESTAMP(6) NOT NULL,
    raw_reference_time TIMESTAMP(6) NOT NULL,
    context_version VARCHAR(64) NOT NULL,
    analysis_engine_version VARCHAR(100) NOT NULL,
    llm_provider VARCHAR(30) NOT NULL,
    llm_model VARCHAR(100) NOT NULL,
    prompt_template_version VARCHAR(50) NOT NULL,
    input_schema_version VARCHAR(50) NOT NULL,
    output_schema_version VARCHAR(50) NOT NULL,
    input_payload_hash VARCHAR(64) NOT NULL,
    input_payload_json TEXT NOT NULL,
    prompt_system_text TEXT NOT NULL,
    prompt_user_text TEXT NOT NULL,
    output_length_policy_json TEXT NOT NULL,
    raw_output_text TEXT NULL,
    output_json TEXT NOT NULL,
    fallback_used BOOLEAN NOT NULL,
    generation_status VARCHAR(20) NOT NULL,
    failure_type VARCHAR(20) NULL,
    validation_issues_json TEXT NOT NULL,
    provider_request_id VARCHAR(120) NULL,
    input_tokens INT NULL,
    output_tokens INT NULL,
    total_tokens INT NULL,
    requested_at TIMESTAMP(6) NOT NULL,
    completed_at TIMESTAMP(6) NOT NULL,
    stored_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_analysis_report_shared_context_identity
        UNIQUE (
            report_type,
            context_version,
            llm_provider,
            llm_model,
            prompt_template_version,
            input_schema_version,
            output_schema_version,
            input_payload_hash
        )
);

CREATE INDEX idx_analysis_report_shared_context_type_basis
    ON analysis_report_shared_context (report_type, analysis_basis_time);

CREATE INDEX idx_analysis_report_shared_context_status_stored_time
    ON analysis_report_shared_context (generation_status, stored_at);
