package com.github.dsquare68.gym;

import com.github.dsquare68.homeforgeapi.dashboard.WidgetDescriptor;
import com.github.dsquare68.homeforgeapi.spi.HubApi;
import com.github.dsquare68.homeforgeapi.spi.HubPlugin;
import com.github.dsquare68.homeforgeapi.spi.PluginIcon;
import com.github.dsquare68.homeforgeapi.spi.PluginMetadata;
import com.github.dsquare68.gym.db.ExerciseNames;
import com.github.dsquare68.gym.db.ExerciseNamesRepository;
import com.github.dsquare68.gym.db.ExerciseSeed;
import com.github.dsquare68.gym.view.DashboardWidget;
import com.github.dsquare68.gym.view.MainView;

import java.util.List;
import com.vaadin.flow.router.RouteConfiguration;

import org.flywaydb.core.Flyway;
import org.pf4j.Extension;

/**
 * HUB lifecycle implementation for this plugin.
 *
 * <p>This is the class you spend most time in. It:
 * <ul>
 *   <li>Declares plugin metadata (id, name, <b>path</b>, <b>schema</b>)</li>
 *   <li>Runs Flyway migrations against the plugin-scoped schema on install</li>
 *   <li>Registers Vaadin routes so the UI becomes reachable at {@link PluginInfo#PLUGIN_PATH}</li>
 *   <li>Optionally contributes a dashboard widget</li>
 * </ul>
 *
 * <p>Plugin identity (id, name, version, description, path, schema) lives in
 * {@link PluginInfo} and must stay in sync with the {@code plugin.path} /
 * {@code plugin.schema} properties in {@code pom.xml}.
 */
@Extension
public class HubPluginImpl implements HubPlugin {

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    private static final System.Logger LOG = System.getLogger(HubPluginImpl.class.getName());

    private HubApi api;

    /** This plugin's own database, from the gym.properties HUB wrote into the jar. */
    private PluginDb db;

    // -----------------------------------------------------------------------
    // HubPlugin SPI
    // -----------------------------------------------------------------------

    /**
     * Returns the plugin's immutable descriptor. HUB reads this once at install time to:
     * <ol>
     *   <li>Register the plugin in the {@code hub_schema.plugins} table</li>
     *   <li>Add a sidebar navigation entry (icon + name linking to {@code path})</li>
     *   <li>Create the PostgreSQL schema named {@code schema} (if not already present)</li>
     * </ol>
     */
    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata(
                PluginInfo.PLUGIN_ID,     // stable snake_case key
                PluginInfo.PLUGIN_NAME,   // sidebar label
                PluginInfo.PLUGIN_VERSION,
                PluginInfo.PLUGIN_DESC,
                PluginInfo.PLUGIN_PATH,   // URL path this plugin owns
                PluginInfo.PLUGIN_SCHEMA  // dedicated PostgreSQL schema
        );
    }

    /**
     * Sidebar / plugin-manager icon.
     *
     * <p>The SPI default reads {@code icon.png} from the jar root; this plugin
     * ships {@link PluginInfo#PLUGIN_ICON} instead, so the lookup has to be
     * pointed at that name explicitly.
     */
    @Override
    public byte[] getIconBytes() {
        return PluginIcon.load(HubPluginImpl.class, PluginInfo.PLUGIN_ICON);
    }

    /**
     * Called once on first install.
     *
     * <p>Run Flyway migrations here so the plugin's tables exist before
     * any user interacts with the plugin. They run against this plugin's own
     * PostgreSQL role — see {@link PluginDb}.
     *
     * <p>The exercise catalogue is seeded straight after, so the app is usable
     * the moment it appears in the sidebar.
     */
    @Override
    public void onInstall(HubApi api) {
        runMigrations();
        seedExerciseNames();
    }

    /**
     * Called every time the plugin is enabled (including after HUB restarts).
     *
     * <p>Re-register dashboard widgets, set up scheduled tasks, etc.
     */
    @Override
    public void onActivate(HubApi api) {
        this.api = api;
        this.db = PluginDb.load();

        // Register a dashboard widget (optional - delete if not needed)
        api.dashboard().registerWidget(
                WidgetDescriptor.builder()
                        .id(PluginInfo.PLUGIN_ID + ".summary")
                        .title(PluginInfo.PLUGIN_NAME)
                        .order(50)
                        .build()
        );
    }

    /**
     * Register Vaadin routes so the UI is reachable at {@link PluginInfo#PLUGIN_PATH}.
     *
     * <p>HUB calls this after {@link #onActivate(HubApi)} and stores the
     * returned registrations so it can remove them on deactivation.
     */
    @Override
    public void registerRoutes(String routes) {
        // Primary view - accessible at PluginInfo.PLUGIN_PATH
        //routes.setRoute(
        //        stripLeadingSlash(PluginInfo.PLUGIN_PATH),
        //        MainView.class
        //);

        // Add more sub-routes here:
        // routes.setRoute(stripLeadingSlash(PluginInfo.PLUGIN_PATH) + "/settings", SettingsView.class);
    }

    /**
     * Called when the plugin is disabled. Remove in-memory resources.
     * Do NOT drop database tables here - use {@link #onUninstall()} for that.
     */
    @Override
    public void onDeactivate() {
        api.dashboard().unregisterWidget(PluginInfo.PLUGIN_ID + ".summary");
        if (db != null) {
            db.close();
            this.db = null;
        }
        this.api = null;
    }

    /**
     * Called when the plugin is permanently removed.
     * Optionally drop the plugin schema here.
     */
    @Override
    public void onUninstall() {
        // Nothing to do: HUB drops this plugin's role and schema when it is
        // removed, along with the gym.properties file inside the jar.
        if (db != null) {
            db.close();
            this.db = null;
        }
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    /**
     * Run Flyway migrations scoped to this plugin's own schema.
     *
     * <p>The DataSource comes from {@link PluginDb} — the credentials HUB
     * generated for this plugin — not from {@code HubApi#storage()}, so the
     * migrations run as the role that owns the schema and cannot touch anything
     * else in the database.
     *
     * <p>Migration scripts live in
     * {@code src/main/resources/db/migration/} and must follow the naming
     * convention {@code V<version>__<description>.sql}.
     */
    private void runMigrations() {
        if (db == null) {
            db = PluginDb.load();
        }

        // Configure with THIS plugin's classloader: Flyway defaults to the
        // thread context classloader, which under PF4J belongs to the HUB
        // host and cannot see db/migration inside the plugin jar.
        Flyway flyway = Flyway.configure(HubPluginImpl.class.getClassLoader())
                .dataSource(db.dataSource())
                // Isolate history table inside the plugin schema
                .table(db.schema() + "_flyway_schema_history")
                // All migration scripts under db/migration/ in the plugin jar
                .locations("classpath:db/migration")
                // HUB already created the schema; this keeps local runs working
                .schemas(db.schema())
                .createSchemas(true)
                .load();

        flyway.migrate();
    }

    /**
     * Load the base exercise set from {@link ExerciseSeed#SEED_RESOURCE} into
     * {@code exercise_names}.
     *
     * <p>The insert skips names that already exist, so this is safe to run more
     * than once: move the call from {@link #onInstall(HubApi)} to
     * {@link #onActivate(HubApi)} if you want a shipped seed-file update to
     * reach installs that already exist. Either way, exercises the user added
     * themselves are never touched.
     *
     * <p>A failure here is logged rather than thrown - an empty catalogue is a
     * recoverable annoyance, and the user can still add exercises by hand.
     */
    private void seedExerciseNames() {
        if (db == null) {
            db = PluginDb.load();
        }

        try {
            List<ExerciseNames> seed = ExerciseSeed.load();
            int inserted = new ExerciseNamesRepository(db).insertMissing(seed);
            LOG.log(System.Logger.Level.INFO,
                    "Exercise catalogue seeded: {0} of {1} rows inserted, the rest were already present.",
                    inserted, seed.size());
        } catch (RuntimeException e) {
            LOG.log(System.Logger.Level.ERROR, "Could not seed the exercise catalogue.", e);
        }
    }

    /** {@code "/my-plugin"} -&gt; {@code "my-plugin"} (Vaadin route format). */
    private static String stripLeadingSlash(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
