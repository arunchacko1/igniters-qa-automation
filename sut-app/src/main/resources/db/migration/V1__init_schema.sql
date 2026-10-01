-- Core schema: users, events, registrations.
-- Foreign keys and unique constraints enforce the business rules at the
-- database level too, not just in application code — the DB tests in
-- qa-tests/.../db verify this directly.

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'MEMBER')),
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE events (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(150) NOT NULL,
    description TEXT         NOT NULL,
    event_date  DATE         NOT NULL,
    location    VARCHAR(150) NOT NULL,
    capacity    INTEGER      NOT NULL CHECK (capacity > 0),
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE registrations (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    -- Intentionally NOT a DB-level foreign key to events. Event deletion is
    -- an application-level operation that must explicitly remove dependent
    -- registrations first (see EventService#deleteEvent). Without a DB FK
    -- as a backstop, skipping that cleanup step produces real orphan rows —
    -- which is exactly the seeded defect #3 (QA_DEFECTS_ENABLED=true). The
    -- LEFT JOIN query in OrphanRegistrationTest is what catches it.
    event_id    BIGINT    NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    -- A member can only register once per event. This is the real guard
    -- against duplicate registrations — the app code checks it too, but
    -- a race between two requests can only be stopped here.
    CONSTRAINT uq_user_event UNIQUE (user_id, event_id)
);

CREATE INDEX idx_events_title ON events (title);
CREATE INDEX idx_events_event_date ON events (event_date);
CREATE INDEX idx_registrations_event_id ON registrations (event_id);
