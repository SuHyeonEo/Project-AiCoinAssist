CREATE TABLE reference_news_snapshot
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    scope                   VARCHAR(40)  NOT NULL,
    snapshot_date           DATE         NOT NULL,
    llm_provider            VARCHAR(30)  NOT NULL,
    llm_model               VARCHAR(100) NOT NULL,
    prompt_template_version VARCHAR(50)  NOT NULL,
    input_schema_version    VARCHAR(50)  NOT NULL,
    output_schema_version   VARCHAR(50)  NOT NULL,
    article_count           INT          NOT NULL,
    input_payload_json      TEXT         NOT NULL,
    prompt_system_text      TEXT         NOT NULL,
    prompt_user_text        TEXT         NOT NULL,
    output_length_policy_json TEXT       NOT NULL,
    raw_output_text         TEXT NULL,
    payload_json            TEXT         NOT NULL,
    provider_request_id     VARCHAR(120) NULL,
    input_tokens            INT NULL,
    output_tokens           INT NULL,
    total_tokens            INT NULL,
    requested_at            DATETIME(6)  NOT NULL,
    completed_at            DATETIME(6)  NOT NULL,
    stored_time             DATETIME(6)  NOT NULL,
    CONSTRAINT uk_reference_news_snapshot_scope_date
        UNIQUE (scope, snapshot_date)
);

CREATE INDEX idx_reference_news_snapshot_scope_date
    ON reference_news_snapshot (scope, snapshot_date);

CREATE INDEX idx_reference_news_snapshot_stored_time
    ON reference_news_snapshot (stored_time);
