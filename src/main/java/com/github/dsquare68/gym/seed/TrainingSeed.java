package com.github.dsquare68.gym.seed;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.dsquare68.gym.entity.TrainingRecord;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Reads the base training schemes (reusable templates) shipped inside the
 * plugin jar.
 *
 * <p>Like {@link ExerciseSeed}, the file is an <em>initial dataset, not a hard
 * dependency</em>, and is parsed through {@link JsonNode} so a later revision can
 * add fields without this loader failing on properties it does not know.
 *
 * <p>Exercises are referenced by their Polish name, not by id - ids are generated
 * on insert, so the caller passes a name-to-id lookup built from the exercise
 * catalogue. An exercise the catalogue does not know is skipped (and reported
 * through {@code onUnknown}) rather than failing the whole scheme.
 *
 * <p>Expected shape:
 * <pre>{@code
 * {
 *   "schema_version": "1.0",
 *   "trainings": [
 *     { "name": "Klata",
 *       "exercises": [
 *         { "order": 1, "exercise_name_pl": "Pompki",
 *           "sets": [ { "set": 1, "reps": 8, "weight_kg": 60.0 } ] } ] } ]
 * }
 * }</pre>
 *
 * <p>The result is one {@link TrainingRecord} per set, flagged
 * {@code IS_SCHEMA=1}. Numbering ({@code ID_TRAINING}, {@code SCHEMA}) is left
 * to the repository, which knows what is already stored.
 */
public final class TrainingSeed {

    /** Classpath resource written into the jar from {@code src/main/resources}. */
    public static final String SEED_RESOURCE = "/db/seed/trainings_schemes_1.1.json";

    private static final String NODE_TRAININGS = "trainings";
    private static final String NODE_EXERCISES = "exercises";
    private static final String NODE_SETS = "sets";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_EXERCISE = "exercise_name_pl";
    private static final String FIELD_SET = "set";
    private static final String FIELD_REPS = "reps";
    private static final String FIELD_WEIGHT = "weight_kg";

    private TrainingSeed() {
    }

    /**
     * Loads every scheme in the seed file, in file order, as not-yet-persisted
     * template rows.
     *
     * @param exerciseIds exercise name to catalogue id
     * @param onUnknown   called with {@code "scheme / exercise"} for each skipped exercise
     * @throws IllegalStateException if the resource is missing or is not the
     *         expected shape, which means the jar was built wrong
     */
    public static List<TrainingRecord> load(Map<String, Long> exerciseIds,
            java.util.function.Consumer<String> onUnknown) {
        try (InputStream in = TrainingSeed.class.getResourceAsStream(SEED_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException(SEED_RESOURCE + " is missing from the plugin jar.");
            }

            JsonNode root = new ObjectMapper().readTree(in);
            JsonNode trainings = root.path(NODE_TRAININGS);
            if (!trainings.isArray()) {
                throw new IllegalStateException(SEED_RESOURCE + " has no '" + NODE_TRAININGS + "' array.");
            }

            List<TrainingRecord> loaded = new ArrayList<>();
            for (JsonNode training : trainings) {
                String schemeName = text(training, FIELD_NAME);
                if (schemeName == null) {
                    continue;
                }
                for (JsonNode exercise : training.path(NODE_EXERCISES)) {
                    String exerciseName = text(exercise, FIELD_EXERCISE);
                    Long exerciseId = exerciseName == null ? null : exerciseIds.get(exerciseName);
                    if (exerciseId == null) {
                        onUnknown.accept(schemeName + " / " + exerciseName);
                        continue;
                    }
                    for (JsonNode set : exercise.path(NODE_SETS)) {
                        loaded.add(new TrainingRecord(
                                null, // ID_TRAINING: assigned by the repository
                                exerciseId,
                                set.path(FIELD_SET).asInt(loaded.size() + 1),
                                set.path(FIELD_REPS).asInt(0),
                                set.path(FIELD_WEIGHT).asDouble(0),
                                0, // SCHEMA: assigned by the repository
                                1,
                                null,
                                schemeName));
                    }
                }
            }
            return loaded;
        } catch (IOException | JacksonException e) {
            throw new IllegalStateException("Could not read " + SEED_RESOURCE, e);
        }
    }

    /** Trimmed text value, or {@code null} when absent, null or blank. */
    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (!value.isString()) {
            return null;
        }
        String text = value.asString().trim();
        return text.isEmpty() ? null : text;
    }
}
