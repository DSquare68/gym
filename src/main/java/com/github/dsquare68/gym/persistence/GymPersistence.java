package com.github.dsquare68.gym.persistence;

import javax.sql.DataSource;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.github.dsquare68.gym.repository.ExerciseNameRepository;
import com.github.dsquare68.gym.repository.TrainingRepository;
import com.github.dsquare68.gym.service.ExerciseNamesServiceImpl;
import com.github.dsquare68.gym.service.TrainingServiceImpl;

/**
 * Owns the plugin's JPA {@link AnnotationConfigApplicationContext} for its whole
 * active lifetime.
 *
 * <p>Built once in {@code HubPluginImpl#onActivate} from {@code db().dataSource()}
 * and {@link #close() closed} in {@code onDeactivate}. The repositories it hands
 * out are the only database entry point the rest of the plugin - views, the
 * external API - ever touches.
 *
 * <p>Views are instantiated by HUB's own Vaadin/Spring machinery, not by this
 * context, so they cannot get {@link #trainingService()} / {@link #exerciseNamesService()}
 * through constructor injection - HUB's application context has no beans for
 * plugin-internal types. {@link #current()} is the same "shared instance"
 * pattern {@code HubPlugin#db()} uses for the datasource: the active instance
 * for the (single) running copy of this plugin, reachable from anywhere in
 * the plugin's own code without wiring.
 */
public final class GymPersistence implements AutoCloseable {

    private static volatile GymPersistence instance;

    private final AnnotationConfigApplicationContext context;

    public GymPersistence(DataSource dataSource) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.setClassLoader(getClass().getClassLoader());
        ctx.getBeanFactory().registerSingleton("gymDataSource", dataSource);
        ctx.register(GymPersistenceConfig.class);

        // Hibernate bootstrap and entity scanning read from the thread context
        // classloader; inside HUB that is HUB's, not this plugin's.
        ClassLoader previous = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try {
            ctx.refresh();
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
        this.context = ctx;
        instance = this;
    }

    /**
     * The active instance for the plugin's own code to reach when it has no
     * other way in - e.g. a Vaadin view HUB instantiates with a no-arg
     * constructor.
     *
     * @throws IllegalStateException if the plugin is not currently activated
     */
    public static GymPersistence current() {
        GymPersistence current = instance;
        if (current == null) {
            throw new IllegalStateException(
                    "Gym plugin's persistence is not active - the plugin must be started first.");
        }
        return current;
    }

    public ExerciseNameRepository exerciseNames() {
        return context.getBean(ExerciseNameRepository.class);
    }

    public TrainingRepository trainings() {
        return context.getBean(TrainingRepository.class);
    }

    public TrainingServiceImpl trainingService() {
        return context.getBean(TrainingServiceImpl.class);
    }

    public ExerciseNamesServiceImpl exerciseNamesService() {
        return context.getBean(ExerciseNamesServiceImpl.class);
    }

    @Override
    public void close() {
        context.close();
        if (instance == this) {
            instance = null;
        }
    }
}
