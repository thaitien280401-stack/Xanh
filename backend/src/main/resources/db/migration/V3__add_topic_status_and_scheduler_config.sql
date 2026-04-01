-- ─── Add status to topics ─────────────────────────────────────────────────────
ALTER TABLE topics
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'DONE'));

CREATE INDEX idx_topics_status ON topics(status);

-- ─── Scheduler config ─────────────────────────────────────────────────────────
CREATE TABLE scheduler_config (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key   VARCHAR(100) NOT NULL UNIQUE,
    config_value VARCHAR(255) NOT NULL,
    description  TEXT,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

INSERT INTO scheduler_config (config_key, config_value, description) VALUES
    ('INTERVAL_HOURS',      '2',    'How often the topic-charge scheduler runs (hours)'),
    ('MIN_VOCAB_THRESHOLD', '10',   'Minimum vocabulary count for a topic to be marked DONE'),
    ('SCHEDULER_ENABLED',   'true', 'Master on/off switch for the scheduler');
