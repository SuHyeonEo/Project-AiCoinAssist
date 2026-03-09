CREATE TABLE market_window_summary_snapshot (
    id BIGINT NOT NULL AUTO_INCREMENT,
    symbol VARCHAR(20) NOT NULL,
    interval_value VARCHAR(20) NOT NULL,
    window_type VARCHAR(20) NOT NULL,
    window_start_time TIMESTAMP(6) NOT NULL,
    window_end_time TIMESTAMP(6) NOT NULL,
    sample_count INT NOT NULL,
    current_price DECIMAL(19, 8) NOT NULL,
    window_high DECIMAL(19, 8) NOT NULL,
    window_low DECIMAL(19, 8) NOT NULL,
    window_range DECIMAL(19, 8) NOT NULL,
    current_position_in_range DECIMAL(19, 8) NULL,
    distance_from_window_high DECIMAL(19, 8) NULL,
    rebound_from_window_low DECIMAL(19, 8) NULL,
    average_volume DECIMAL(19, 8) NOT NULL,
    average_atr DECIMAL(19, 8) NOT NULL,
    current_volume DECIMAL(19, 8) NOT NULL,
    current_atr DECIMAL(19, 8) NOT NULL,
    current_volume_vs_average DECIMAL(19, 8) NULL,
    current_atr_vs_average DECIMAL(19, 8) NULL,
    source_data_version VARCHAR(300) NOT NULL,
    CONSTRAINT pk_market_window_summary_snapshot PRIMARY KEY (id),
    CONSTRAINT uk_market_window_summary_snapshot_symbol_interval_window_end
        UNIQUE (symbol, interval_value, window_type, window_end_time)
);

CREATE INDEX idx_market_window_summary_snapshot_symbol_interval_window_end
    ON market_window_summary_snapshot (symbol, interval_value, window_end_time);

CREATE INDEX idx_market_window_summary_snapshot_source_data_version
    ON market_window_summary_snapshot (source_data_version);
