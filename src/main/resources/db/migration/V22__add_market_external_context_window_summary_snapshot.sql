CREATE TABLE market_external_context_window_summary_snapshot
(
    id                                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol                             VARCHAR(20)    NOT NULL,
    window_type                        VARCHAR(20)    NOT NULL,
    window_start_time                  TIMESTAMP(6)   NOT NULL,
    window_end_time                    TIMESTAMP(6)   NOT NULL,
    sample_count                       INT            NOT NULL,
    current_composite_risk_score       DECIMAL(19, 8) NOT NULL,
    average_composite_risk_score       DECIMAL(19, 8) NOT NULL,
    current_composite_risk_vs_average  DECIMAL(19, 8) NULL,
    supportive_dominance_sample_count  INT            NOT NULL,
    cautionary_dominance_sample_count  INT            NOT NULL,
    headwind_dominance_sample_count    INT            NOT NULL,
    high_severity_sample_count         INT            NOT NULL,
    source_data_version                VARCHAR(500)   NOT NULL,
    CONSTRAINT uk_market_external_context_window_summary_symbol_window_end
        UNIQUE (symbol, window_type, window_end_time)
);

CREATE INDEX idx_market_external_context_window_summary_symbol_window_end
    ON market_external_context_window_summary_snapshot (symbol, window_end_time);

CREATE INDEX idx_market_external_context_window_summary_source_data_version
    ON market_external_context_window_summary_snapshot (source_data_version);
