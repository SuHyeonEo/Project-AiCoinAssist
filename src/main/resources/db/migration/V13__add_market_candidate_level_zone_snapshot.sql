CREATE TABLE market_candidate_level_zone_snapshot (
    id BIGINT NOT NULL AUTO_INCREMENT,
    symbol VARCHAR(20) NOT NULL,
    interval_value VARCHAR(20) NOT NULL,
    snapshot_time TIMESTAMP(6) NOT NULL,
    zone_type VARCHAR(20) NOT NULL,
    zone_rank INT NOT NULL,
    current_price DECIMAL(19, 8) NOT NULL,
    representative_price DECIMAL(19, 8) NOT NULL,
    zone_low DECIMAL(19, 8) NOT NULL,
    zone_high DECIMAL(19, 8) NOT NULL,
    distance_from_current DECIMAL(19, 8) NULL,
    zone_strength_score DECIMAL(19, 8) NULL,
    strongest_level_label VARCHAR(20) NOT NULL,
    strongest_source_type VARCHAR(30) NOT NULL,
    level_count INT NOT NULL,
    included_level_labels_payload TEXT NOT NULL,
    included_source_types_payload TEXT NOT NULL,
    trigger_facts_payload TEXT NOT NULL,
    source_data_version VARCHAR(500) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_market_candidate_level_zone_snapshot_symbol_interval_zone_rank
        UNIQUE (symbol, interval_value, snapshot_time, zone_type, zone_rank)
);

CREATE INDEX idx_market_candidate_level_zone_snapshot_symbol_interval_snapshot
    ON market_candidate_level_zone_snapshot (symbol, interval_value, snapshot_time);

CREATE INDEX idx_market_candidate_level_zone_snapshot_source_data_version
    ON market_candidate_level_zone_snapshot (source_data_version);
