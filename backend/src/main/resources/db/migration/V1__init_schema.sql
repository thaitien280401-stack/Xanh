-- ─── Users ────────────────────────────────────────────────────
CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    avatar_url  TEXT,
    role        VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email    ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- ─── Refresh Tokens ───────────────────────────────────────────
CREATE TABLE refresh_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token      VARCHAR(512) NOT NULL UNIQUE,
    expires_at TIMESTAMP    NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_token_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_token_token   ON refresh_tokens(token);

-- ─── Topics ───────────────────────────────────────────────────
CREATE TABLE topics (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name             VARCHAR(100) NOT NULL UNIQUE,
    description      TEXT,
    external_api_ref VARCHAR(255),
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_topics_name ON topics(name);

-- ─── Vocabularies ─────────────────────────────────────────────
CREATE TABLE vocabularies (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    topic_id         UUID         NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    word             VARCHAR(255) NOT NULL,
    definition       TEXT,
    pronunciation    VARCHAR(255),
    part_of_speech   VARCHAR(50),
    example_sentence TEXT,
    audio_url        TEXT,
    image_url        TEXT,
    difficulty       VARCHAR(10)  NOT NULL DEFAULT 'MEDIUM',
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_vocabularies_topic_id ON vocabularies(topic_id);
CREATE INDEX idx_vocabularies_word     ON vocabularies(word);

-- ─── Pomodoro Sessions ────────────────────────────────────────
CREATE TABLE pomodoro_sessions (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    topic_id         UUID      NOT NULL REFERENCES topics(id),
    status           VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    duration_minutes INTEGER   NOT NULL,
    words_studied    INTEGER   NOT NULL DEFAULT 0,
    started_at       TIMESTAMP NOT NULL,
    ended_at         TIMESTAMP,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pomodoro_user_id    ON pomodoro_sessions(user_id);
CREATE INDEX idx_pomodoro_topic_id   ON pomodoro_sessions(topic_id);
CREATE INDEX idx_pomodoro_started_at ON pomodoro_sessions(started_at);

-- ─── User Vocabulary Progress ─────────────────────────────────
CREATE TABLE user_vocabulary_progress (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vocabulary_id    UUID      NOT NULL REFERENCES vocabularies(id) ON DELETE CASCADE,
    times_seen       INTEGER   NOT NULL DEFAULT 0,
    times_correct    INTEGER   NOT NULL DEFAULT 0,
    mastery_level    VARCHAR(20) NOT NULL DEFAULT 'NEW',
    last_reviewed_at TIMESTAMP,
    next_review_at   TIMESTAMP,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_progress_user_vocab UNIQUE (user_id, vocabulary_id)
);

CREATE INDEX idx_progress_user_id       ON user_vocabulary_progress(user_id);
CREATE INDEX idx_progress_vocabulary_id ON user_vocabulary_progress(vocabulary_id);
CREATE INDEX idx_progress_next_review   ON user_vocabulary_progress(next_review_at);

-- ─── Quizzes ──────────────────────────────────────────────────
CREATE TABLE quizzes (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    topic_id          UUID          NOT NULL REFERENCES topics(id),
    quiz_type         VARCHAR(20)   NOT NULL,
    total_questions   INTEGER       NOT NULL,
    correct_answers   INTEGER       NOT NULL DEFAULT 0,
    score             DECIMAL(5, 2),
    time_taken_seconds INTEGER,
    completed_at      TIMESTAMP,
    created_at        TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_quiz_user_id      ON quizzes(user_id);
CREATE INDEX idx_quiz_topic_id     ON quizzes(topic_id);
CREATE INDEX idx_quiz_completed_at ON quizzes(completed_at);

-- ─── Quiz Questions ───────────────────────────────────────────
CREATE TABLE quiz_questions (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id        UUID NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    vocabulary_id  UUID NOT NULL REFERENCES vocabularies(id),
    question_text  TEXT NOT NULL,
    correct_answer TEXT NOT NULL,
    options        TEXT,
    user_answer    TEXT,
    is_correct     BOOLEAN,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_quiz_question_quiz_id  ON quiz_questions(quiz_id);
CREATE INDEX idx_quiz_question_vocab_id ON quiz_questions(vocabulary_id);

-- ─── Leaderboard Entries ──────────────────────────────────────
CREATE TABLE leaderboard_entries (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID          NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    total_words_learned INTEGER       NOT NULL DEFAULT 0,
    total_study_minutes INTEGER       NOT NULL DEFAULT 0,
    total_quizzes       INTEGER       NOT NULL DEFAULT 0,
    highest_quiz_score  DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    weekly_points       INTEGER       NOT NULL DEFAULT 0,
    total_points        INTEGER       NOT NULL DEFAULT 0,
    last_updated_at     TIMESTAMP,
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_leaderboard_total_points  ON leaderboard_entries(total_points DESC);
CREATE INDEX idx_leaderboard_weekly_points ON leaderboard_entries(weekly_points DESC);
