package com.github.dsquare68.gym;

import com.github.dsquare68.homeforgeapi.dashboard.WidgetDescriptor;
import com.github.dsquare68.homeforgeapi.spi.HubApi;
import com.github.dsquare68.homeforgeapi.spi.HubPlugin;
import com.github.dsquare68.homeforgeapi.spi.PluginIcon;
import com.github.dsquare68.homeforgeapi.spi.PluginMetadata;
import com.github.dsquare68.homeforgeapi.spi.PluginRoute;
import com.github.dsquare68.gym.entity.ExerciseName;
import com.github.dsquare68.gym.persistence.GymPersistence;
import com.github.dsquare68.gym.seed.ExerciseSeed;
import com.github.dsquare68.gym.view.MainView;

import java.util.List;

import org.pf4j.Extension;

/**
 * HUB lifecycle implementation for this plugin.
 *
 * <p>It:
 * <ul>
 *   <li>Declares plugin metadata (id, name, <b>path</b>, <b>schema</b>)</li>
 *   <li>Stands up the plugin's JPA context ({@link GymPersistence}) over
 *       {@link #db()} on activation, and closes it on deactivation</li>
 *   <li>Seeds the exercise catalogue from the file shipped in the jar</li>
 *   <li>Registers Vaadin routes so the UI becomes reachable at {@link PluginInfo#PLUGIN_PATH}</li>
 *   <li>Contributes a dashboard widget</li>
 * </ul>
 *
 * <p>The database schema itself is created by Hibernate from the {@code @Entity}
 * classes the first time {@link GymPersistence} starts - there is no separate DDL
 * step here.
 */
@Extension
public class HubPluginImpl implements HubPlugin {

    // -----------------------------------------------------------------------
    // State
    // -----------------------------------------------------------------------

    private static final System.Logger LOG = System.getLogger(HubPluginImpl.class.getName());

    private HubApi api;
    private GymPersistence persistence;

    // -----------------------------------------------------------------------
    // HubPlugin SPI
    // -----------------------------------------------------------------------

    @Override
    public PluginMetadata getMetadata() {
        return new PluginMetadata(
                PluginInfo.PLUGIN_ID,
                PluginInfo.PLUGIN_NAME,
                PluginInfo.PLUGIN_VERSION,
                PluginInfo.PLUGIN_DESC,
                PluginInfo.PLUGIN_PATH,
                PluginInfo.PLUGIN_SCHEMA);
    }

    @Override
    public byte[] getIconBytes() {
        return PluginIcon.load(HubPluginImpl.class, PluginInfo.PLUGIN_ICON);
    }

    /**
     * Called once when HUB first installs the jar: bring the database up and fill
     * the exercise catalogue, so the plugin is usable the moment it appears in
     * the sidebar.
     */
    @Override
    public void onInstall(HubApi api) {
        prepareDatabase();
    }

    /**
     * Called every time the plugin is enabled (including after HUB restarts).
     *
     * <p>{@link #prepareDatabase()} runs here as well as on install. Both steps
     * are idempotent, and HUB does not call {@code onInstall} again when an
     * existing plugin is upgraded to a new jar - so this is what lets a version
     * that adds a mapped column, or ships more exercises, take effect on a
     * machine the plugin is already installed on.
     */
    @Override
    public void onActivate(HubApi api) {
        this.api = api;

        prepareDatabase();

        api.dashboard().registerWidget(
                WidgetDescriptor.builder()
                        .id(PluginInfo.PLUGIN_ID + ".summary")
                        .title(PluginInfo.PLUGIN_NAME)
                        .order(50)
                        .build());
    }

    @Override
    public List<PluginRoute> routes() {
        return List.of(new PluginRoute("", MainView.class));
    }

    /**
     * Called when the plugin is disabled. Closes the JPA context - the pool
     * behind {@link #db()} is HUB's to shut down, but the {@code EntityManagerFactory}
     * stacked on top of it is this plugin's.
     */
    @Override
    public void onDeactivate() {
        if (api != null) {
            api.dashboard().unregisterWidget(PluginInfo.PLUGIN_ID + ".summary");
        }
        closePersistence();
        this.api = null;
    }

    @Override
    public void onUninstall() {
        // Nothing to do: HUB drops this plugin's role and schema when it is
        // removed, along with the credentials file inside the jar and the
        // connection pool behind db().
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    /**
     * Brings the database up to what this version of the jar expects: the JPA
     * context is running (which is what creates or extends the tables), and the
     * exercise catalogue holds at least what the seed file ships.
     *
     * <p>Safe to call repeatedly.
     */
    private synchronized void prepareDatabase() {
        if (persistence == null) {
            persistence = new GymPersistence(db().dataSource());
        }
        seedExerciseNames();
    }

    /**
     * Loads the base exercise set from {@link ExerciseSeed#SEED_RESOURCE} into
     * {@code exercise_names}, skipping names that are already there.
     *
     * <p>A failure here is logged rather than thrown - an empty catalogue is a
     * recoverable annoyance, and the user can still add exercises by hand.
     */
    private void seedExerciseNames() {
        try {
            List<ExerciseName> seed = ExerciseSeed.load();
            int inserted = persistence.exerciseNames().saveMissing(seed);
            LOG.log(System.Logger.Level.INFO,
                    "Exercise catalogue seeded: {0} of {1} rows inserted, the rest were already present.",
                    inserted, seed.size());
        } catch (RuntimeException e) {
            LOG.log(System.Logger.Level.ERROR, "Could not seed the exercise catalogue.", e);
        }
    }

    private synchronized void closePersistence() {
        if (persistence != null) {
            try {
                persistence.close();
            } catch (RuntimeException e) {
                LOG.log(System.Logger.Level.WARNING, "Could not close the JPA context cleanly.", e);
            }
            persistence = null;
        }
    }
}
