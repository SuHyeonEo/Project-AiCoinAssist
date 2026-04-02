CREATE TABLE market_candidate_level_snapshot (
    id BIGINT NOT NULL AUTO_INCREMENT,
    symbol VARCHAR(20) NOT NULL,
    interval_value VARCHAR(20) NOT NULL,
    snapshot_time TIMESTAMP(6) NOT NULL,
    level_type VARCHAR(20) NOT NULL,
    level_label VARCHAR(20) NOT NULL,
    source_type VARCHAR(30) NOT NULL,
    current_price DECIMAL(19, 8) NOT NULL,
    level_price DECIMAL(19, 8) NOT NULL,
    distance_from_current DECIMAL(19, 8) NULL,
    strength_score DECIMAL(19, 8) NULL,
    rationale VARCHAR(200) NOT NULL,
    trigger_facts_payload TEXT NOT NULL,
    source_data_version VARCHAR(300) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_market_candidate_level_snapshot_key
        UNIQUE (symbol, interval_value, snapshot_time, level_type, level_label)
);

CREATE INDEX idx_market_candidate_level_snapshot_symbol_interval_snapshot
    ON market_candidate_level_snapshot (symbol, interval_value, snapshot_time);

CREATE INDEX idx_market_candidate_level_snapshot_source_data_version
    ON market_candidate_level_snapshot (source_data_version);
