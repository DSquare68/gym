package com.github.dsquare68.gym.db;

/**
 * One row of {@code gym_schema.exercise_names} - the plugin's exercise catalogue.
 *
 * <p>Rows come from two places and are otherwise identical: the seed file
 * ({@link Source#SEED}) and exercises the user adds through the app
 * ({@link Source#USER}). {@code name} is unique across both.
 *
 * @param id                  database identity; {@code 0} for a row that has not been inserted yet
 * @param name                unique exercise name, e.g. {@code "Incline Dumbbell Bench Press"}
 * @param category            grouping used by the exercise picker, e.g. {@code "Chest"}
 * @param mainMuscleWorking   primary muscle group, e.g. {@code "Pectoralis major"}
 * @param source              where the row came from
 */
public record ExerciseNames(
        long id,
        String name,
        String category,
        String mainMuscleWorking,
        Source source) {

    /** Whether a row was seeded from file or created by the user. */
    public enum Source {
        SEED("seed"),
        USER("user");

        private final String column;

        Source(String column) {
            this.column = column;
        }

        /** The value stored in the {@code source} column. */
        public String column() {
            return column;
        }

        static Source fromColumn(String value) {
            return USER.column.equals(value) ? USER : SEED;
        }
    }

    public ExerciseNames {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Exercise name must not be blank");
        }
    }

    /** A not-yet-persisted row read from the seed file. */
    public static ExerciseNames seeded(String name, String category, String mainMuscleWorking) {
        return new ExerciseNames(0L, name, category, mainMuscleWorking, Source.SEED);
    }

    /** A not-yet-persisted row the user added through the app. */
    public static ExerciseNames added(String name, String category, String mainMuscleWorking) {
        return new ExerciseNames(0L, name, category, mainMuscleWorking, Source.USER);
    }
}
