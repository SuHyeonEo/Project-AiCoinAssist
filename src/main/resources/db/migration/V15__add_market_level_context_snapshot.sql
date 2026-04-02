CREATE TABLE market_level_context_snapshot
(
    id                           BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol                       VARCHAR(20)    NOT NULL,
    interval_value               VARCHAR(20)    NOT NULL,
    snapshot_time                TIMESTAMP(6)   NOT NULL,
    current_price                DECIMAL(19, 8) NOT NULL,
    support_zone_rank            INT            NULL,
    support_representative_price DECIMAL(19, 8) NULL,
    support_zone_low             DECIMAL(19, 8) NULL,
    support_zone_high            DECIMAL(19, 8) NULL,
    support_distance_to_zone     DECIMAL(19, 8) NULL,
    support_zone_strength        DECIMAL(19, 8) NULL,
    support_interaction_type     VARCHAR(20)    NULL,
    support_recent_test_count    INT            NULL,
    support_recent_rejection_count INT          NULL,
    support_recent_break_count   INT            NULL,
    support_break_risk           DECIMAL(19, 8) NULL,
    resistance_zone_rank         INT            NULL,
    resistance_representative_price DECIMAL(19, 8) NULL,
    resistance_zone_low          DECIMAL(19, 8) NULL,
    resistance_zone_high         DECIMAL(19, 8) NULL,
    resistance_distance_to_zone  DECIMAL(19, 8) NULL,
    resistance_zone_strength     DECIMAL(19, 8) NULL,
    resistance_interaction_type  VARCHAR(20)    NULL,
    resistance_recent_test_count INT            NULL,
    resistance_recent_rejection_count INT       NULL,
    resistance_recent_break_count INT           NULL,
    resistance_break_risk        DECIMAL(19, 8) NULL,
    source_data_version          TEXT           NOT NULL,
    CONSTRAINT uk_market_level_context_snapshot_symbol_interval_snapshot_time UNIQUE (symbol, interval_value, snapshot_time)
);

CREATE INDEX idx_market_level_context_snapshot_symbol_interval_snapshot_time
    ON market_level_context_snapshot (symbol, interval_value, snapshot_time);
