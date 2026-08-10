-- ============================================================
-- V3__trainings.sql
-- Training schemes and the sessions performed against them.
--
-- One table holds both. A scheme (template = TRUE) is the reusable
-- structure - the exercises and how many rounds each has, with the reps
-- and weight columns left NULL. A session (template = FALSE) is a copy of
-- that structure with the numbers the user actually filled in, pointing
-- back at the scheme it came from.
--
-- The alternative - separate scheme_* and session_* tables - removes the
-- NULLs but duplicates the exercise/round structure and every query over
-- it. Kept as one table because a session is genuinely the same shape as
-- the scheme it was built from; split it if schemes grow fields that a
-- session has no use for (target rep ranges, progression rules, ...).
-- ============================================================

CREATE TABLE IF NOT EXISTS gym_schema.trainings
(
    id           BIGSERIAL    PRIMARY KEY,
    user_id      UUID         NOT NULL,          -- references hub_schema.users.id (logical FK)
    name         VARCHAR(255) NOT NULL,

    -- TRUE  = a reusable scheme / plan
    -- FALSE = a session someone actually performed
    template     BOOLEAN      NOT NULL DEFAULT FALSE,

    -- Which scheme this session was built from. NULL for a scheme itself,
    -- and for a one-off session logged without a plan.
    scheme_id    BIGINT       REFERENCES gym_schema.trainings (id) ON DELETE SET NULL,

    -- When it was performed. NULL on a scheme, which has no date.
    performed_on DATE,
    started_at   TIME,
    notes        TEXT,

    -- How the row arrived: the Vaadin UI, or a client posting over the
    -- future external API. Nothing branches on it yet - it is here so the
    -- API seam does not need a migration to become traceable.
    source       VARCHAR(16)  NOT NULL DEFAULT 'web',

    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),

    -- A scheme has no date; a performed session must have one.
    CONSTRAINT ck_trainings_template_has_no_date
        CHECK ((template AND performed_on IS NULL) OR (NOT template AND performed_on IS NOT NULL))
);

-- The history view: this user's sessions, newest first.
CREATE INDEX IF NOT EXISTS idx_trainings_user_performed
    ON gym_schema.trainings (user_id, performed_on DESC);

-- "Which sessions came from this scheme"
CREATE INDEX IF NOT EXISTS idx_trainings_scheme
    ON gym_schema.trainings (scheme_id);


-- ------------------------------------------------------------
-- The exercises inside one training, in the order they are done.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS gym_schema.training_exercises
(
    id               BIGSERIAL PRIMARY KEY,
    training_id      BIGINT    NOT NULL REFERENCES gym_schema.trainings (id) ON DELETE CASCADE,

    -- No ON DELETE: an exercise that has been performed cannot be deleted
    -- out from under its own history.
    exercise_name_id BIGINT    NOT NULL REFERENCES gym_schema.exercise_names (id),

    position         INT       NOT NULL,
    notes            TEXT,

    CONSTRAINT uq_training_exercises_position UNIQUE (training_id, position)
);

-- Drives "every set I have ever done of this exercise".
CREATE INDEX IF NOT EXISTS idx_training_exercises_exercise
    ON gym_schema.training_exercises (exercise_name_id);


-- ------------------------------------------------------------
-- One set: what the user types in.
--
-- reps and weight are NULL on a scheme, where the row means only "this
-- exercise has a 4th round" - the numbers arrive when the session is filled.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS gym_schema.training_rounds
(
    id                   BIGSERIAL    PRIMARY KEY,
    training_exercise_id BIGINT       NOT NULL
                                      REFERENCES gym_schema.training_exercises (id) ON DELETE CASCADE,
    round_number         INT          NOT NULL,
    reps                 INT,
    weight               NUMERIC(6,2),

    CONSTRAINT uq_training_rounds_number UNIQUE (training_exercise_id, round_number),
    CONSTRAINT ck_training_rounds_reps   CHECK (reps   IS NULL OR reps   >= 0),
    CONSTRAINT ck_training_rounds_weight CHECK (weight IS NULL OR weight >= 0)
);
