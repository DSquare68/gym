package com.github.dsquare68.gym.db;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One performed set, flattened out of its training - the shape history and
 * personal-record screens want.
 *
 * <p>Deliberately not a {@link TrainingRound}: those belong to a training and
 * carry no date, whereas everything asked of a set after the fact ("when",
 * "in which session", "was that a record") needs the session around it.
 *
 * @param performedOn  the date of the session this set belongs to
 * @param trainingId   the session, for linking back to it
 * @param trainingName the session's name
 * @param exerciseName the exercise performed
 * @param roundNumber  which set within the exercise
 * @param reps         repetitions performed
 * @param weight       kilograms
 */
public record PerformedSet(
        LocalDate performedOn,
        long trainingId,
        String trainingName,
        String exerciseName,
        int roundNumber,
        int reps,
        BigDecimal weight) {

    /** Rough one-rep-max, Epley: {@code weight * (1 + reps/30)}. */
    public BigDecimal estimatedOneRepMax() {
        return weight.multiply(BigDecimal.valueOf(30 + reps))
                .divide(BigDecimal.valueOf(30), 2, java.math.RoundingMode.HALF_UP);
    }
}
