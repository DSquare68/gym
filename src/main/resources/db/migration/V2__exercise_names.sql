-- ============================================================
-- V2__exercise_names.sql
-- Exercise catalogue for the Gym Tracker plugin.
--
-- Seeded on first start from db/seed/exercises_merged_final_1.0.json;
-- users add their own rows afterwards through the app, so seeded and
-- user-added exercises live side by side in this one table.
-- ============================================================

CREATE TABLE IF NOT EXISTS gym_schema.exercise_names
(
    id                  BIGSERIAL    PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    category            VARCHAR(64),
    main_muscle_working VARCHAR(128),
    -- 'seed' = came from the seed file, 'user' = added through the app.
    -- Lets a later seed version refresh its own rows without touching
    -- anything the user created.
    source              VARCHAR(16)  NOT NULL DEFAULT 'seed',
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),

    -- The seed file guarantees unique names; this keeps the start up
    -- procedure idempotent (re-running it inserts nothing new) and makes
    -- name the natural lookup key from the UI and the future external API.
    CONSTRAINT uq_exercise_names_name UNIQUE (name)
);

-- Category is how the exercise picker groups its list.
CREATE INDEX IF NOT EXISTS idx_exercise_names_category
    ON gym_schema.exercise_names (category);
