package com.github.dsquare68.gym.db;

import java.util.ArrayList;
import java.util.List;

/**
 * One exercise inside a training, together with its sets.
 *
 * <p>Carries both {@code exerciseNameId} and {@code exerciseName}: the id is
 * the foreign key that gets written, the name is what the UI shows and what an
 * external client would send, since name is the catalogue's unique key. Either
 * one is enough to save - see
 * {@link TrainingRepository#insert(Training)}, which resolves a name to its id
 * when the id is missing.
 *
 * @param id             database identity; {@code 0} before insert
 * @param exerciseNameId foreign key into {@code exercise_names}; {@code 0} when only the name is known
 * @param exerciseName   the catalogue name, e.g. {@code "Incline Dumbbell Bench Press"}
 * @param position       0-based order within the training
 * @param notes          free text, or {@code null}
 * @param rounds         the sets, in round order
 */
public record TrainingExercise(
        long id,
        long exerciseNameId,
        String exerciseName,
        int position,
        String notes,
        List<TrainingRound> rounds) {

    public TrainingExercise {
        if (exerciseNameId == 0L && (exerciseName == null || exerciseName.isBlank())) {
            throw new IllegalArgumentException("An exercise needs either an id or a name");
        }
        if (position < 0) {
            throw new IllegalArgumentException("Position is 0-based, got " + position);
        }
        rounds = rounds == null ? List.of() : List.copyOf(rounds);
    }

    /** An exercise with {@code roundCount} empty sets, for building a scheme. */
    public static TrainingExercise planned(String exerciseName, int position, int roundCount) {
        List<TrainingRound> rounds = new ArrayList<>(roundCount);
        for (int round = 1; round <= roundCount; round++) {
            rounds.add(TrainingRound.planned(round));
        }
        return new TrainingExercise(0L, 0L, exerciseName, position, null, rounds);
    }

    /** The same exercise with a different set of rounds - used when filling a session in. */
    public TrainingExercise withRounds(List<TrainingRound> replacement) {
        return new TrainingExercise(id, exerciseNameId, exerciseName, position, notes, replacement);
    }
}
