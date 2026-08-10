package com.github.dsquare68.gym.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import com.github.dsquare68.gym.PluginDb;

/**
 * Reads and writes schemes and sessions over this plugin's own connection.
 *
 * <p>{@link #insert(Training)} writes the whole graph - training, exercises,
 * rounds - in one transaction, so a half-saved workout is not a state this
 * plugin can reach.
 *
 * <p>Nothing here touches Vaadin or {@code HubApi}: every method takes the
 * owning user id as a parameter. That is what keeps the external-API seam
 * honest - an Android client posting a session goes through exactly this class,
 * not a second write path that has to be kept in step.
 */
public final class TrainingRepository {

    /** Guards against building SQL from an unexpected schema name. */
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private final DataSource dataSource;
    private final String trainings;
    private final String trainingExercises;
    private final String trainingRounds;
    private final String exerciseNames;

    /**
     * Takes the connection and schema directly, so this class is not tied to
     * one way of getting them - {@link PluginDb} today, the platform's
     * {@code PluginDbConnection} or a test datasource just as well.
     */
    public TrainingRepository(DataSource dataSource, String schema) {
        if (!SAFE_IDENTIFIER.matcher(schema).matches()) {
            throw new IllegalArgumentException("Refusing to build SQL for schema name: " + schema);
        }
        this.dataSource = dataSource;
        this.trainings = schema + ".trainings";
        this.trainingExercises = schema + ".training_exercises";
        this.trainingRounds = schema + ".training_rounds";
        this.exerciseNames = schema + ".exercise_names";
    }

    public TrainingRepository(PluginDb db) {
        this(db.dataSource(), db.schema());
    }

    // -----------------------------------------------------------------------
    // Writing
    // -----------------------------------------------------------------------

    /**
     * Saves a scheme or a filled-in session, with its exercises and rounds.
     *
     * <p>Exercises may name their exercise instead of carrying its id; the name
     * is resolved against the catalogue here. An unknown name is rejected rather
     * than quietly added, so a typo from any client - the UI or a phone pushing
     * a session - cannot grow the catalogue by accident. Add it deliberately
     * through {@link ExerciseNamesRepository#add(ExerciseNames)} first.
     *
     * @return the same training with database ids filled in
     * @throws IllegalStateException if an exercise name is not in the catalogue
     */
    public Training insert(Training training) {
        try (Connection connection = dataSource.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                long trainingId = insertTraining(connection, training);

                List<TrainingExercise> saved = new ArrayList<>(training.exercises().size());
                for (TrainingExercise exercise : training.exercises()) {
                    saved.add(insertExercise(connection, trainingId, exercise));
                }

                connection.commit();
                return new Training(trainingId, training.userId(), training.name(), training.template(),
                        training.schemeId(), training.performedOn(), training.startedAt(),
                        training.notes(), training.source(), saved);
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not save training '" + training.name() + "'", e);
        }
    }

    private long insertTraining(Connection connection, Training training) throws SQLException {
        String sql = "INSERT INTO " + trainings
                + " (user_id, name, template, scheme_id, performed_on, started_at, notes, source)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                + " RETURNING id";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, training.userId());
            statement.setString(2, training.name());
            statement.setBoolean(3, training.template());
            setNullableLong(statement, 4, training.schemeId());
            statement.setObject(5, training.performedOn(), Types.DATE);
            statement.setObject(6, training.startedAt(), Types.TIME);
            statement.setString(7, training.notes());
            statement.setString(8, training.source().column());

            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    throw new IllegalStateException("Insert of training '" + training.name() + "' returned no id");
                }
                return rows.getLong(1);
            }
        }
    }

    private TrainingExercise insertExercise(Connection connection, long trainingId, TrainingExercise exercise)
            throws SQLException {

        long exerciseNameId = exercise.exerciseNameId() != 0L
                ? exercise.exerciseNameId()
                : resolveExerciseNameId(connection, exercise.exerciseName());

        String sql = "INSERT INTO " + trainingExercises
                + " (training_id, exercise_name_id, position, notes)"
                + " VALUES (?, ?, ?, ?)"
                + " RETURNING id";

        long exerciseId;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, trainingId);
            statement.setLong(2, exerciseNameId);
            statement.setInt(3, exercise.position());
            statement.setString(4, exercise.notes());

            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    throw new IllegalStateException(
                            "Insert of exercise '" + exercise.exerciseName() + "' returned no id");
                }
                exerciseId = rows.getLong(1);
            }
        }

        insertRounds(connection, exerciseId, exercise.rounds());

        return new TrainingExercise(exerciseId, exerciseNameId, exercise.exerciseName(),
                exercise.position(), exercise.notes(), exercise.rounds());
    }

    private void insertRounds(Connection connection, long exerciseId, List<TrainingRound> rounds)
            throws SQLException {

        if (rounds.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO " + trainingRounds
                + " (training_exercise_id, round_number, reps, weight)"
                + " VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (TrainingRound round : rounds) {
                statement.setLong(1, exerciseId);
                statement.setInt(2, round.roundNumber());
                if (round.reps() == null) {
                    statement.setNull(3, Types.INTEGER);
                } else {
                    statement.setInt(3, round.reps());
                }
                statement.setBigDecimal(4, round.weight());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private long resolveExerciseNameId(Connection connection, String name) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT id FROM " + exerciseNames + " WHERE name = ?")) {
            statement.setString(1, name);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    throw new IllegalStateException("'" + name + "' is not in the exercise catalogue."
                            + " Add it before saving a training that uses it.");
                }
                return rows.getLong(1);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Reading
    // -----------------------------------------------------------------------

    /** This user's reusable schemes, newest first. Headers only - no exercises. */
    public List<Training> findSchemes(UUID userId) {
        return findHeaders("WHERE user_id = ? AND template = TRUE ORDER BY created_at DESC",
                statement -> statement.setObject(1, userId));
    }

    /**
     * This user's performed sessions, newest first.
     *
     * @param schemeId restrict to sessions built from one scheme, or {@code null} for all
     */
    public List<Training> findSessions(UUID userId, Long schemeId) {
        if (schemeId == null) {
            return findHeaders("WHERE user_id = ? AND template = FALSE ORDER BY performed_on DESC, id DESC",
                    statement -> statement.setObject(1, userId));
        }
        return findHeaders(
                "WHERE user_id = ? AND template = FALSE AND scheme_id = ? ORDER BY performed_on DESC, id DESC",
                statement -> {
                    statement.setObject(1, userId);
                    statement.setLong(2, schemeId);
                });
    }

    /**
     * One training with everything under it, for editing or for
     * {@link Training#fillFrom(Training, LocalDate)}.
     */
    public Optional<Training> findById(long id) {
        String sql = "SELECT t.id, t.user_id, t.name, t.template, t.scheme_id, t.performed_on,"
                + "       t.started_at, t.notes, t.source,"
                + "       te.id AS exercise_id, te.exercise_name_id, te.position, te.notes AS exercise_notes,"
                + "       en.name AS exercise_name,"
                + "       tr.id AS round_id, tr.round_number, tr.reps, tr.weight"
                + " FROM " + trainings + " t"
                + " LEFT JOIN " + trainingExercises + " te ON te.training_id = t.id"
                + " LEFT JOIN " + exerciseNames + " en ON en.id = te.exercise_name_id"
                + " LEFT JOIN " + trainingRounds + " tr ON tr.training_exercise_id = te.id"
                + " WHERE t.id = ?"
                + " ORDER BY te.position, tr.round_number";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return assemble(rows);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read training " + id, e);
        }
    }

    /**
     * Every set this user has performed of one exercise, newest first.
     *
     * <p>Only sets with both numbers filled in - a planned-but-never-entered
     * round is not history.
     */
    public List<PerformedSet> history(UUID userId, String exerciseName) {
        String sql = performedSetQuery() + " ORDER BY t.performed_on DESC, te.position, tr.round_number";
        return performedSets(sql, userId, exerciseName);
    }

    /**
     * The heaviest set of one exercise - the personal record.
     *
     * <p>Ties on weight are broken by reps, then by the earlier date, so the
     * record is the first time it was achieved.
     */
    public Optional<PerformedSet> personalRecord(UUID userId, String exerciseName) {
        String sql = performedSetQuery()
                + " ORDER BY tr.weight DESC, tr.reps DESC, t.performed_on ASC"
                + " LIMIT 1";
        List<PerformedSet> best = performedSets(sql, userId, exerciseName);
        return best.isEmpty() ? Optional.empty() : Optional.of(best.get(0));
    }

    // -----------------------------------------------------------------------
    // Internals
    // -----------------------------------------------------------------------

    /** Shared projection behind {@link #history} and {@link #personalRecord}. */
    private String performedSetQuery() {
        return "SELECT t.performed_on, t.id AS training_id, t.name AS training_name,"
                + "       en.name AS exercise_name, tr.round_number, tr.reps, tr.weight"
                + " FROM " + trainingRounds + " tr"
                + " JOIN " + trainingExercises + " te ON te.id = tr.training_exercise_id"
                + " JOIN " + trainings + " t ON t.id = te.training_id"
                + " JOIN " + exerciseNames + " en ON en.id = te.exercise_name_id"
                + " WHERE t.user_id = ? AND en.name = ? AND t.template = FALSE"
                + "   AND tr.reps IS NOT NULL AND tr.weight IS NOT NULL";
    }

    private List<PerformedSet> performedSets(String sql, UUID userId, String exerciseName) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, userId);
            statement.setString(2, exerciseName);

            try (ResultSet rows = statement.executeQuery()) {
                List<PerformedSet> sets = new ArrayList<>();
                while (rows.next()) {
                    sets.add(new PerformedSet(
                            rows.getObject("performed_on", LocalDate.class),
                            rows.getLong("training_id"),
                            rows.getString("training_name"),
                            rows.getString("exercise_name"),
                            rows.getInt("round_number"),
                            rows.getInt("reps"),
                            rows.getBigDecimal("weight")));
                }
                return sets;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read history for '" + exerciseName + "'", e);
        }
    }



    /** Binds the parameters of a header query. */
    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    private List<Training> findHeaders(String where, Binder binder) {
        String sql = "SELECT id, user_id, name, template, scheme_id, performed_on, started_at, notes, source"
                + " FROM " + trainings + " " + where;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            binder.bind(statement);
            try (ResultSet rows = statement.executeQuery()) {
                List<Training> found = new ArrayList<>();
                while (rows.next()) {
                    found.add(header(rows, List.of()));
                }
                return found;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read from " + trainings, e);
        }
    }

    /** Folds the joined rows of {@link #findById(long)} back into one object. */
    private Optional<Training> assemble(ResultSet rows) throws SQLException {
        Training training = null;
        List<TrainingExercise> exercises = new ArrayList<>();

        long currentExerciseId = 0L;
        long currentExerciseNameId = 0L;
        String currentExerciseName = null;
        int currentPosition = 0;
        String currentNotes = null;
        List<TrainingRound> currentRounds = new ArrayList<>();

        while (rows.next()) {
            if (training == null) {
                training = header(rows, List.of());
            }

            long exerciseId = rows.getLong("exercise_id");
            if (rows.wasNull()) {
                continue; // training with no exercises yet
            }

            if (exerciseId != currentExerciseId) {
                if (currentExerciseId != 0L) {
                    exercises.add(new TrainingExercise(currentExerciseId, currentExerciseNameId,
                            currentExerciseName, currentPosition, currentNotes, currentRounds));
                }
                currentExerciseId = exerciseId;
                currentExerciseNameId = rows.getLong("exercise_name_id");
                currentExerciseName = rows.getString("exercise_name");
                currentPosition = rows.getInt("position");
                currentNotes = rows.getString("exercise_notes");
                currentRounds = new ArrayList<>();
            }

            long roundId = rows.getLong("round_id");
            if (!rows.wasNull()) {
                currentRounds.add(new TrainingRound(roundId, rows.getInt("round_number"),
                        nullableInt(rows, "reps"), rows.getBigDecimal("weight")));
            }
        }

        if (training == null) {
            return Optional.empty();
        }
        if (currentExerciseId != 0L) {
            exercises.add(new TrainingExercise(currentExerciseId, currentExerciseNameId,
                    currentExerciseName, currentPosition, currentNotes, currentRounds));
        }
        return Optional.of(training.withExercises(exercises));
    }

    private static Training header(ResultSet rows, List<TrainingExercise> exercises) throws SQLException {
        long schemeId = rows.getLong("scheme_id");
        // wasNull() reports on the most recent read, so it has to be captured
        // here - by the time the constructor runs, template has been read since.
        Long scheme = rows.wasNull() ? null : schemeId;

        return new Training(
                rows.getLong("id"),
                rows.getObject("user_id", UUID.class),
                rows.getString("name"),
                rows.getBoolean("template"),
                scheme,
                rows.getObject("performed_on", LocalDate.class),
                rows.getObject("started_at", LocalTime.class),
                rows.getString("notes"),
                Training.Source.fromColumn(rows.getString("source")),
                exercises);
    }

    private static Integer nullableInt(ResultSet rows, String column) throws SQLException {
        int value = rows.getInt(column);
        return rows.wasNull() ? null : value;
    }

    private static void setNullableLong(PreparedStatement statement, int index, Long value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.BIGINT);
        } else {
            statement.setLong(index, value);
        }
    }
}
