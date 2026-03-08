CREATE TABLE analysis_report (
    id BIGINT NOT NULL AUTO_INCREMENT,
    symbol VARCHAR(20) NOT NULL,
    report_type VARCHAR(20) NOT NULL,
    analysis_basis_time TIMESTAMP(6) NOT NULL,
    raw_reference_time TIMESTAMP(6) NOT NULL,
    source_data_version VARCHAR(200) NOT NULL,
    analysis_engine_version VARCHAR(100) NOT NULL,
    report_payload TEXT NOT NULL,
    stored_time TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_analysis_report PRIMARY KEY (id),
    CONSTRAINT uk_analysis_report_symbol_type_basis_version_engine
        UNIQUE (symbol, report_type, analysis_basis_time, source_data_version, analysis_engine_version)
);

CREATE INDEX idx_analysis_report_symbol_type_basis_time
    ON analysis_report (symbol, report_type, analysis_basis_time);

CREATE INDEX idx_analysis_report_raw_reference_time
    ON analysis_report (raw_reference_time);
