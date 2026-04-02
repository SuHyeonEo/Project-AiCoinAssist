CREATE TABLE market_context_window_summary_snapshot (
    id BIGINT NOT NULL AUTO_INCREMENT,
    symbol VARCHAR(20) NOT NULL,
    window_type VARCHAR(20) NOT NULL,
    window_start_time TIMESTAMP(6) NOT NULL,
    window_end_time TIMESTAMP(6) NOT NULL,
    sample_count INT NOT NULL,
    current_open_interest DECIMAL(19, 8) NOT NULL,
    average_open_interest DECIMAL(19, 8) NOT NULL,
    current_open_interest_vs_average DECIMAL(19, 8) NULL,
    current_funding_rate DECIMAL(19, 8) NOT NULL,
    average_funding_rate DECIMAL(19, 8) NOT NULL,
    current_funding_vs_average DECIMAL(19, 8) NULL,
    current_basis_rate DECIMAL(19, 8) NOT NULL,
    average_basis_rate DECIMAL(19, 8) NOT NULL,
    current_basis_vs_average DECIMAL(19, 8) NULL,
    source_data_version VARCHAR(300) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_market_context_window_summary_symbol_window_end
        UNIQUE (symbol, window_type, window_end_time)
);

CREATE INDEX idx_market_context_window_summary_symbol_window_end
    ON market_context_window_summary_snapshot (symbol, window_end_time);

CREATE INDEX idx_market_context_window_summary_source_data_version
    ON market_context_window_summary_snapshot (source_data_version);
