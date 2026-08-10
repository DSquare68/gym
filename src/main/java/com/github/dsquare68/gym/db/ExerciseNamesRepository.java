package com.github.dsquare68.gym.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import com.github.dsquare68.gym.PluginDb;

/**
 * Reads and writes {@code exercise_names} over this plugin's own connection.
 *
 * <p>Plain JDBC on purpose: the parent pom ships Hikari and the PostgreSQL
 * driver but no JPA provider, and this table is small and queried in exactly
 * two ways. Add a mapper later if the training/session tables need one.
 *
 * <p>Every method is safe to call from any client - the Vaadin UI today, the
 * external API this plugin is expected to grow later - because uniqueness and
 * conflict handling live in SQL, not in a caller-side check.
 */
public final class ExerciseNamesRepository {

    /** Guards against building SQL from an unexpected schema name. */
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    private final DataSource dataSource;
    private final String table;

    /**
     * Takes the connection and schema directly, so this class is not tied to
     * one way of getting them - {@link PluginDb} today, the platform's
     * {@code PluginDbConnection} or a test datasource just as well.
     */
    public ExerciseNamesRepository(DataSource dataSource, String schema) {
        if (!SAFE_IDENTIFIER.matcher(schema).matches()) {
            throw new IllegalArgumentException("Refusing to build SQL for schema name: " + schema);
        }
        this.dataSource = dataSource;
        this.table = schema + ".exercise_names";
    }

    public ExerciseNamesRepository(PluginDb db) {
        this(db.dataSource(), db.schema());
    }

    /**
     * Inserts the seed set, skipping names that are already there.
     *
     * <p>Idempotent by way of the {@code uq_exercise_names_name} constraint, so
     * re-running it after an upgrade adds only what is genuinely new and never
     * touches a row the user edited or created.
     *
     * @return how many rows were actually inserted
     */
    public int insertMissing(List<ExerciseNames> exercises) {
        if (exercises.isEmpty()) {
            return 0;
        }

        String sql = "INSERT INTO " + table + " (name, category, main_muscle_working, source)"
                + " VALUES (?, ?, ?, ?)"
                + " ON CONFLICT (name) DO NOTHING";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (ExerciseNames exercise : exercises) {
                bindInsert(statement, exercise);
                statement.addBatch();
            }

            int inserted = 0;
            for (int updated : statement.executeBatch()) {
                // Skipped rows report 0; drivers may report SUCCESS_NO_INFO (-2).
                if (updated > 0) {
                    inserted += updated;
                }
            }
            return inserted;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not insert exercise names into " + table, e);
        }
    }

    /**
     * Adds a single exercise the user created in the app.
     *
     * @return the stored row, with its generated id
     * @throws IllegalStateException if the name is already taken
     */
    public ExerciseNames add(ExerciseNames exercise) {
        String sql = "INSERT INTO " + table + " (name, category, main_muscle_working, source)"
                + " VALUES (?, ?, ?, ?)"
                + " RETURNING id, name, category, main_muscle_working, source";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            bindInsert(statement, exercise);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    throw new IllegalStateException("Insert of '" + exercise.name() + "' returned no row");
                }
                return map(rows);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not add exercise '" + exercise.name() + "'", e);
        }
    }

    /** The whole catalogue - seeded and user-added alike - grouped for the picker. */
    public List<ExerciseNames> findAll() {
        String sql = "SELECT id, name, category, main_muscle_working, source"
                + " FROM " + table
                + " ORDER BY category, name";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {

            List<ExerciseNames> all = new ArrayList<>();
            while (rows.next()) {
                all.add(map(rows));
            }
            return all;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read " + table, e);
        }
    }

    /** Lookup by the unique name - the key other clients will reference an exercise by. */
    public Optional<ExerciseNames> findByName(String name) {
        String sql = "SELECT id, name, category, main_muscle_working, source"
                + " FROM " + table
                + " WHERE name = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(map(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not look up exercise '" + name + "'", e);
        }
    }

    /** How many rows the catalogue holds - used by the dashboard widget. */
    public long count() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT count(*) FROM " + table)) {

            return rows.next() ? rows.getLong(1) : 0L;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not count rows in " + table, e);
        }
    }

    private static void bindInsert(PreparedStatement statement, ExerciseNames exercise) throws SQLException {
        statement.setString(1, exercise.name());
        statement.setString(2, exercise.category());
        statement.setString(3, exercise.mainMuscleWorking());
        statement.setString(4, exercise.source().column());
    }

    private static ExerciseNames map(ResultSet rows) throws SQLException {
        return new ExerciseNames(
                rows.getLong("id"),
                rows.getString("name"),
                rows.getString("category"),
                rows.getString("main_muscle_working"),
                ExerciseNames.Source.fromColumn(rows.getString("source")));
    }
}
