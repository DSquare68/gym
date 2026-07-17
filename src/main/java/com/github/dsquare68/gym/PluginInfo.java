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

    public static final String PLUGIN_VERSION = "${pluginVersion}";

    public static final String PLUGIN_DESC = "${pluginDescription}";

    public static final String TITLE = "Gym Tracker";

    private PluginInfo() {
    }
}
