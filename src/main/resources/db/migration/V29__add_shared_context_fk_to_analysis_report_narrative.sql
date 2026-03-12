ALTER TABLE analysis_report_narrative
    ADD COLUMN shared_context_id BIGINT NULL AFTER analysis_report_id;

ALTER TABLE analysis_report_narrative
    ADD CONSTRAINT fk_analysis_report_narrative_shared_context
        FOREIGN KEY (shared_context_id) REFERENCES analysis_report_shared_context (id);

CREATE INDEX idx_analysis_report_narrative_shared_context_id
    ON analysis_report_narrative (shared_context_id);
