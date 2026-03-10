ALTER TABLE market_candidate_level_zone_snapshot
    ADD COLUMN distance_to_zone DECIMAL(19, 8) NULL;

ALTER TABLE market_candidate_level_zone_snapshot
    ADD COLUMN interaction_type VARCHAR(20) NOT NULL DEFAULT 'INSIDE_ZONE';

ALTER TABLE market_candidate_level_zone_snapshot
    ADD COLUMN recent_test_count INT NOT NULL DEFAULT 0;

ALTER TABLE market_candidate_level_zone_snapshot
    ADD COLUMN recent_rejection_count INT NOT NULL DEFAULT 0;

ALTER TABLE market_candidate_level_zone_snapshot
    ADD COLUMN recent_break_count INT NOT NULL DEFAULT 0;
