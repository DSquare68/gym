package com.github.dsquare68.gym.persistence;

import javax.sql.DataSource;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.github.dsquare68.gym.repository.ExerciseNameRepository;
import com.github.dsquare68.gym.repository.TrainingRepository;

/**
 * Owns the plugin's JPA {@link AnnotationConfigApplicationContext} for its whole
 * active lifetime.
 *
 * <p>Built once in {@code HubPluginImpl#onActivate} from {@code db().dataSource()}
 * and {@link #close() closed} in {@code onDeactivate}. The repositories it hands
 * out are the only database entry point the rest of the plugin - views, the
 * external API - ever touches.
 */
public final class GymPersistence implements AutoCloseable {

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
    }

    public ExerciseNameRepository exerciseNames() {
        return context.getBean(ExerciseNameRepository.class);
    }

    public TrainingRepository trainings() {
        return context.getBean(TrainingRepository.class);
    }

    @Override
    public void close() {
        context.close();
    }
}
