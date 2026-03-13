DROP INDEX IF EXISTS idx_analysis_report_shared_context_type_basis
    ON analysis_report_shared_context;

CREATE INDEX idx_analysis_report_shared_context_basis
    ON analysis_report_shared_context (analysis_basis_time);
