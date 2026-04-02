ALTER TABLE analysis_report_batch_run
    ADD COLUMN trigger_type VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED' AFTER execution_status;

ALTER TABLE analysis_report_batch_run
    ADD COLUMN rerun_source_run_id VARCHAR(100) NULL AFTER trigger_type;

CREATE INDEX idx_analysis_report_batch_run_rerun_source_run_id
    ON analysis_report_batch_run (rerun_source_run_id);
