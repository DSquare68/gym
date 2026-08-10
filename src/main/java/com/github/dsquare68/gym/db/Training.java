package com.github.dsquare68.gym.db;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A training scheme, or a session performed against one.
 *
 * <p>Both are this one type, told apart by {@link #template()}:
 * <ul>
 *   <li>{@code template = true} - a reusable plan. It has exercises and round
 *       counts but no date and no numbers.</li>
 *   <li>{@code template = false} - something the user actually did. It has a
 *       date, filled-in rounds, and usually a {@link #schemeId()} pointing at
 *       the plan it came from.</li>
 * </ul>
 *
 * <p>{@link #fillFrom(Training, LocalDate)} is the bridge between the two: it
 * turns a scheme into an empty session for the user to type into.
 *
 * @param id           database identity; {@code 0} before insert
 * @param userId       owning HUB user, from {@code hubApi.user().currentUserId()}
 * @param name         what the user calls it, e.g. {@code "Push A"}
 * @param template     {@code true} for a scheme, {@code false} for a performed session
 * @param schemeId     the scheme this session came from, or {@code null}
 * @param performedOn  the date it was done; {@code null} on a scheme
 * @param startedAt    time of day it started, or {@code null}
 * @param notes        free text, or {@code null}
 * @param source       which client wrote it
 * @param exercises    the exercises, in {@code position} order
 */
public record Training(
        long id,
        UUID userId,
        String name,
        boolean template,
        Long schemeId,
        LocalDate performedOn,
        LocalTime startedAt,
        String notes,
        Source source,
        List<TrainingExercise> exercises) {

    /** Which client wrote the row - mirrors the {@code source} column. */
    public enum Source {
        /** Entered through the plugin's own Vaadin UI. */
        WEB("web"),
        /** Pushed in by an external client over the API. */
        API("api");

        private final String column;

        Source(String column) {
            this.column = column;
        }

        public String column() {
            return column;
        }

        static Source fromColumn(String value) {
            return API.column.equals(value) ? API : WEB;
        }
    }

    public Training {
        if (userId == null) {
            throw new IllegalArgumentException("A training must belong to a user");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A training must have a name");
        }
        // Mirrors ck_trainings_template_has_no_date, so a bad object fails here
        // rather than as a constraint violation three layers down.
        if (template && performedOn != null) {
            throw new IllegalArgumentException("A scheme has no date, got " + performedOn);
        }
        if (!template && performedOn == null) {
            throw new IllegalArgumentException("A performed session needs a date");
        }
        source = source == null ? Source.WEB : source;
        exercises = exercises == null ? List.of() : List.copyOf(exercises);
    }

    /** A reusable plan - exercises and round counts, no numbers. */
    public static Training scheme(UUID userId, String name, List<TrainingExercise> exercises) {
        return new Training(0L, userId, name, true, null, null, null, null, Source.WEB, exercises);
    }

    /** A session logged directly, without a scheme behind it. */
    public static Training session(UUID userId, String name, LocalDate performedOn,
                                   List<TrainingExercise> exercises) {
        return new Training(0L, userId, name, false, null, performedOn, null, null, Source.WEB, exercises);
    }

    /**
     * Turns a scheme into an empty session for {@code performedOn}, ready for
     * the user to fill in.
     *
     * <p>The structure is copied, not shared: same exercises in the same order
     * with the same number of rounds, every round blank. Saving the result
     * leaves the scheme untouched, so the same plan can be run every week.
     *
     * @throws IllegalArgumentException if {@code scheme} is not a template
     */
    public static Training fillFrom(Training scheme, LocalDate performedOn) {
        if (!scheme.template()) {
            throw new IllegalArgumentException(
                    "Sessions are filled from a scheme, but training " + scheme.id() + " is not one");
        }

        List<TrainingExercise> blank = new ArrayList<>(scheme.exercises().size());
        for (TrainingExercise exercise : scheme.exercises()) {
            List<TrainingRound> rounds = new ArrayList<>(exercise.rounds().size());
            for (TrainingRound round : exercise.rounds()) {
                rounds.add(TrainingRound.planned(round.roundNumber()));
            }
            blank.add(new TrainingExercise(
                    0L, exercise.exerciseNameId(), exercise.exerciseName(),
                    exercise.position(), null, rounds));
        }

        return new Training(0L, scheme.userId(), scheme.name(), false, scheme.id(),
                performedOn, null, null, Source.WEB, blank);
    }

    /** The same training with its exercises replaced - used as the user edits. */
    public Training withExercises(List<TrainingExercise> replacement) {
        return new Training(id, userId, name, template, schemeId, performedOn,
                startedAt, notes, source, replacement);
    }

    /** Marks this as having arrived over the external API rather than the UI. */
    public Training viaApi() {
        return new Training(id, userId, name, template, schemeId, performedOn,
                startedAt, notes, Source.API, exercises);
    }
}
