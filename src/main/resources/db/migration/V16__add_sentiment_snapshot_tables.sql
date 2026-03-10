CREATE TABLE sentiment_snapshot_raw
(
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    source                    VARCHAR(50)    NOT NULL,
    metric_type               VARCHAR(50)    NOT NULL,
    source_event_time         TIMESTAMP(6)   NULL,
    collected_time            TIMESTAMP(6)   NOT NULL,
    validation_status         VARCHAR(20)    NOT NULL,
    validation_details        TEXT           NULL,
    index_value               DECIMAL(19, 8) NULL,
    classification            VARCHAR(50)    NULL,
    time_until_update_seconds BIGINT         NULL,
    raw_payload               TEXT           NOT NULL,
    CONSTRAINT uk_sentiment_snapshot_raw_source_metric_source_event_time UNIQUE (source, metric_type, source_event_time)
);

CREATE INDEX idx_sentiment_snapshot_raw_metric_collected_time
    ON sentiment_snapshot_raw (metric_type, collected_time);

CREATE TABLE sentiment_snapshot
(
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,
    metric_type               VARCHAR(50)    NOT NULL,
    snapshot_time             TIMESTAMP(6)   NOT NULL,
    source_event_time         TIMESTAMP(6)   NOT NULL,
    source_data_version       VARCHAR(200)   NOT NULL,
    index_value               DECIMAL(19, 8) NOT NULL,
    classification            VARCHAR(50)    NOT NULL,
    time_until_update_seconds BIGINT         NULL,
    previous_snapshot_time    TIMESTAMP(6)   NULL,
    previous_index_value      DECIMAL(19, 8) NULL,
    value_change              DECIMAL(19, 8) NULL,
    value_change_rate         DECIMAL(19, 8) NULL,
    classification_changed    BOOLEAN        NULL,
    CONSTRAINT uk_sentiment_snapshot_metric_snapshot_time UNIQUE (metric_type, snapshot_time)
);

CREATE INDEX idx_sentiment_snapshot_metric_snapshot_time
    ON sentiment_snapshot (metric_type, snapshot_time);

CREATE INDEX idx_sentiment_snapshot_source_data_version
    ON sentiment_snapshot (source_data_version);
