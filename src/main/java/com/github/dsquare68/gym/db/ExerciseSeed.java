package com.github.dsquare68.gym.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Reads the base exercise set shipped inside the plugin jar.
 *
 * <p>The file is an <em>initial dataset, not a hard dependency</em>: the
 * catalogue is a normal table that users keep adding to, so nothing here
 * assumes the seed is the complete picture.
 *
 * <p>Parsing goes through {@link JsonNode} rather than data binding on purpose.
 * The seed format is not locked in, so a later revision can add fields (or a
 * whole new top-level key) without this loader throwing on properties it does
 * not recognise - it reads the three it needs and ignores the rest.
 *
 * <p>Expected shape:
 * <pre>{@code
 * {
 *   "schema_version": "1.0",
 *   "fields": { ... },
 *   "exercises": [
 *     { "name": "Bench Press", "category": "Chest", "main_muscle_working": "Pectoralis major" }
 *   ]
 * }
 * }</pre>
 */
public final class ExerciseSeed {

    /** Classpath resource written into the jar from {@code src/main/resources}. */
    public static final String SEED_RESOURCE = "/db/seed/exercises_merged_final_1.0.json";

    private static final String NODE_EXERCISES = "exercises";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_CATEGORY = "category";
    private static final String FIELD_MAIN_MUSCLE = "main_muscle_working";

    private ExerciseSeed() {
    }

    /**
     * Loads every exercise in the seed file, in file order.
     *
     * <p>Entries without a usable {@code name} are skipped rather than failing
     * the whole load - one bad record should not stop the plugin installing.
     *
     * @throws IllegalStateException if the resource is missing or is not the
     *         expected shape, which means the jar was built wrong
     */
    public static List<ExerciseNames> load() {
        try (InputStream in = ExerciseSeed.class.getResourceAsStream(SEED_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException(SEED_RESOURCE + " is missing from the plugin jar.");
            }

            JsonNode root = new ObjectMapper().readTree(in);
            JsonNode exercises = root.path(NODE_EXERCISES);
            if (!exercises.isArray()) {
                throw new IllegalStateException(SEED_RESOURCE + " has no '" + NODE_EXERCISES + "' array.");
            }

            List<ExerciseNames> loaded = new ArrayList<>(exercises.size());
            for (JsonNode exercise : exercises) {
                String name = text(exercise, FIELD_NAME);
                if (name == null) {
                    continue;
                }
                loaded.add(ExerciseNames.seeded(
                        name,
                        text(exercise, FIELD_CATEGORY),
                        text(exercise, FIELD_MAIN_MUSCLE)));
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
