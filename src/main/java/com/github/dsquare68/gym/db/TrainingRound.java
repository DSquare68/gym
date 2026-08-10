package com.github.dsquare68.gym.db;

import java.math.BigDecimal;

/**
 * One set of one exercise - the smallest thing the user types in.
 *
 * <p>{@code reps} and {@code weight} are nullable because the same row means
 * two things: on a scheme it says only "this exercise has a 4th round", and on
 * a performed session it carries what was actually lifted.
 *
 * <p>Weight is {@link BigDecimal}, matching {@code NUMERIC(6,2)}. Doubles would
 * be easier to bind to a number field, but history screens group sets by equal
 * weight and {@code 62.5} arrived at two different ways is exactly where that
 * goes wrong.
 *
 * @param id          database identity; {@code 0} before insert
 * @param roundNumber 1-based position of this set within the exercise
 * @param reps        repetitions performed, or {@code null} when not filled in yet
 * @param weight      kilograms, or {@code null} when not filled in yet
 */
public record TrainingRound(long id, int roundNumber, Integer reps, BigDecimal weight) {

    public TrainingRound {
        if (roundNumber < 1) {
            throw new IllegalArgumentException("Round number is 1-based, got " + roundNumber);
        }
        if (reps != null && reps < 0) {
            throw new IllegalArgumentException("Reps must not be negative, got " + reps);
        }
        if (weight != null && weight.signum() < 0) {
            throw new IllegalArgumentException("Weight must not be negative, got " + weight);
        }
    }

    /** An empty set: the structure exists, the numbers do not yet. */
    public static TrainingRound planned(int roundNumber) {
        return new TrainingRound(0L, roundNumber, null, null);
    }

    /** A set the user filled in. */
    public static TrainingRound performed(int roundNumber, Integer reps, BigDecimal weight) {
        return new TrainingRound(0L, roundNumber, reps, weight);
    }

    /** Whether the user has actually entered numbers for this set. */
    public boolean isFilled() {
        return reps != null && weight != null;
    }
}
