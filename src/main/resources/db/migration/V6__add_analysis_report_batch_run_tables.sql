CREATE TABLE analysis_report_batch_run (
    id BIGINT NOT NULL AUTO_INCREMENT,
    run_id VARCHAR(100) NOT NULL,
    execution_status VARCHAR(30) NOT NULL,
    engine_version VARCHAR(100) NOT NULL,
    started_at TIMESTAMP(6) NOT NULL,
    finished_at TIMESTAMP(6) NOT NULL,
    duration_millis BIGINT NOT NULL,
    asset_success_count INT NOT NULL,
    asset_failure_count INT NOT NULL,
    stored_time TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_analysis_report_batch_run PRIMARY KEY (id),
    CONSTRAINT uk_analysis_report_batch_run_run_id UNIQUE (run_id)
);

CREATE INDEX idx_analysis_report_batch_run_started_at
    ON analysis_report_batch_run (started_at);

CREATE INDEX idx_analysis_report_batch_run_status
    ON analysis_report_batch_run (execution_status);

CREATE TABLE analysis_report_batch_asset_result (
    id BIGINT NOT NULL AUTO_INCREMENT,
    batch_run_id BIGINT NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    execution_status VARCHAR(30) NOT NULL,
    started_at TIMESTAMP(6) NOT NULL,
    finished_at TIMESTAMP(6) NOT NULL,
    duration_millis BIGINT NOT NULL,
    snapshot_success_count INT NOT NULL,
    snapshot_failure_count INT NOT NULL,
    report_success_count INT NOT NULL,
    report_failure_count INT NOT NULL,
    crash_error_message TEXT NULL,
    snapshot_results_payload TEXT NOT NULL,
    report_results_payload TEXT NOT NULL,
    stored_time TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_analysis_report_batch_asset_result PRIMARY KEY (id),
    CONSTRAINT fk_analysis_report_batch_asset_result_run
        FOREIGN KEY (batch_run_id) REFERENCES analysis_report_batch_run (id),
    CONSTRAINT uk_analysis_report_batch_asset_result_run_symbol
        UNIQUE (batch_run_id, symbol)
);

CREATE INDEX idx_analysis_report_batch_asset_result_run_id
    ON analysis_report_batch_asset_result (batch_run_id);

CREATE INDEX idx_analysis_report_batch_asset_result_symbol_status
    ON analysis_report_batch_asset_result (symbol, execution_status);
