package com.github.dsquare68.gym;

/**
 * Central plugin identity constants - keep in sync with the
 * {@code plugin.path} / {@code plugin.schema} properties in {@code pom.xml}.
 */
public final class PluginInfo {

    /** Must match {@code <plugin.path>} in pom.xml. */
    public static final String PLUGIN_PATH = "/gym";

    /** Must match {@code <plugin.schema>} in pom.xml. */
    public static final String PLUGIN_SCHEMA = "gym_schema";

    /** Stable snake_case identifier - also used as the Flyway schema history table prefix. */
    public static final String PLUGIN_ID = "gym";

    public static final String PLUGIN_NAME = "Gym Tracker";

    public static final String PLUGIN_VERSION = "0.0.1-SNAPSHOT";

    public static final String PLUGIN_DESC = "Track workouts, personal records and progress charts.";

    public static final String TITLE = "Gym Tracker";
    
    /**
     * Classpath location of the sidebar/plugin-manager icon, relative to
     * {@code src/main/resources/}. The file ships empty - replace it with a real
     * PNG, or point this at another one and keep the name you prefer.
     */
    public static final String PLUGIN_ICON = "gym.png";

    private PluginInfo() {
    }
}
