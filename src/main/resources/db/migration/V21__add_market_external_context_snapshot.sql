CREATE TABLE market_external_context_snapshot (
    id BIGINT NOT NULL AUTO_INCREMENT,
    symbol VARCHAR(20) NOT NULL,
    snapshot_time TIMESTAMP(6) NOT NULL,
    derivative_snapshot_time TIMESTAMP(6) NULL,
    macro_snapshot_time TIMESTAMP(6) NULL,
    sentiment_snapshot_time TIMESTAMP(6) NULL,
    onchain_snapshot_time TIMESTAMP(6) NULL,
    source_data_version VARCHAR(500) NOT NULL,
    composite_risk_score DECIMAL(19,8) NOT NULL,
    dominant_direction VARCHAR(20) NULL,
    highest_severity VARCHAR(20) NULL,
    supportive_signal_count INT NOT NULL,
    cautionary_signal_count INT NOT NULL,
    headwind_signal_count INT NOT NULL,
    primary_signal_category VARCHAR(20) NULL,
    primary_signal_title VARCHAR(120) NULL,
    primary_signal_detail TEXT NULL,
    regime_signals_payload TEXT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_market_external_context_snapshot_symbol_snapshot_time UNIQUE (symbol, snapshot_time)
);

CREATE INDEX idx_market_external_context_snapshot_symbol_snapshot_time
    ON market_external_context_snapshot (symbol, snapshot_time);

CREATE INDEX idx_market_external_context_snapshot_source_data_version
    ON market_external_context_snapshot (source_data_version);
