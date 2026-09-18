package com.github.dsquare68.gym.persistence;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.github.dsquare68.gym.entity.ExerciseName;
import com.github.dsquare68.gym.repository.ExerciseNameRepository;
import com.github.dsquare68.gym.repository.TrainingRepository;
import com.github.dsquare68.gym.service.ExerciseNamesServiceImpl;

import jakarta.persistence.EntityManagerFactory;

/**
 * The plugin's own JPA context.
 *
 * <p>HUB plugins get no Spring container of their own - the SPI is deliberately
 * Spring-free (see {@code HubPlugin#restControllers()}). So this plugin stands up
 * a small {@link org.springframework.context.ApplicationContext} internally,
 * around the {@code DataSource} from {@code HubPlugin#db()}: a Hibernate
 * {@link EntityManagerFactory}, a {@link JpaTransactionManager}, and the Spring
 * Data JPA repositories under {@code com.github.dsquare68.gym.repository}.
 *
 * <p>Classloading: Spring Data JPA and Hibernate are bundled into the plugin jar;
 * Spring Framework core ({@code spring-context}, {@code spring-beans}, ...) is
 * provided by the HUB host, exactly as {@code spring-web} already is. The two
 * sets load from different classloaders but there is only one copy of each type,
 * so nothing clashes across the PF4J boundary.
 *
 * <p>Schema management is Hibernate's ({@code hbm2ddl.auto=update}): the tables
 * are derived from the {@code @Entity} classes on first run. The tradeoff -
 * {@code update} adds columns and tables but never rewrites or drops one - is
 * acceptable while the model is still exploratory; the day a change has to
 * backfill existing rows, add Flyway against the same {@code db().dataSource()}
 * and switch this to {@code validate}.
 *
 * <p>Also component-scans {@code com.github.dsquare68.gym.service}, so the
 * {@code @Service} classes there (which depend on the repositories above via
 * {@code @Autowired}) become real beans of this context too - see
 * {@link GymPersistence#trainingService()} / {@link GymPersistence#exerciseNamesService()}.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses = {ExerciseNameRepository.class,TrainingRepository.class})
@ComponentScan(basePackageClasses = ExerciseNamesServiceImpl.class)
public class GymPersistenceConfig {

    /** Entities are scanned from the package that holds {@link ExerciseName}. */
    private static final String ENTITY_PACKAGE = ExerciseName.class.getPackageName();

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource gymDataSource) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(gymDataSource);
        factory.setPersistenceUnitName("gym");
        factory.setPackagesToScan(ENTITY_PACKAGE);
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaProperties(hibernateProperties());
        return factory;
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    private static Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        // The DataSource is already pinned to the plugin's schema; say so to
        // Hibernate too, so generated DDL and metadata lookups are unambiguous.
        properties.setProperty("hibernate.jakarta.persistence.schema-generation.database.action", "update");
        return properties;
    }
}
