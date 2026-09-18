package com.github.dsquare68.gym.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.dsquare68.gym.entity.ExerciseName;

/**
 * The exercise catalogue - seeded from file, then grown by the user.
 *
 * <p>A Spring Data JPA repository: the connection, transaction and SQL are all
 * managed by the persistence context wired in
 * {@code com.github.dsquare68.gym.persistence.GymPersistenceConfig}. Callers -
 * the Vaadin UI today, an external API client later - go through exactly this
 * interface, never a hand-rolled connection.
 */
public interface ExerciseNameRepository extends JpaRepository<ExerciseName, Long> {

    /** Lookup by the unique name - the key other clients reference an exercise by. */
    Optional<ExerciseName> findByName(String name);

    boolean existsByName(String name);

    /** The whole catalogue - seeded and user-added alike - grouped for the picker. */
    List<ExerciseName> findAllByOrderByCategoryAscNameAsc();

    /**
     * Persists the seed set, skipping names that are already there.
     *
     * <p>Idempotent by way of the {@code uq_exercise_names_name} constraint plus
     * this pre-check, so re-running it after an upgrade adds only what is
     * genuinely new and never touches a row the user edited or created.
     *
     * @return how many rows were actually inserted
     */
    default int saveMissing(List<ExerciseName> candidates) {
        if (candidates.isEmpty()) {
            return 0;
        }
        Set<String> present = findAll().stream()
                .map(ExerciseName::getName)
                .collect(Collectors.toSet());
        List<ExerciseName> missing = candidates.stream()
                .filter(candidate -> !present.contains(candidate.getName()))
                .toList();
        saveAll(missing);
        return missing.size();
    }
    
	@Query(value = "SELECT MAX(ID) FROM gym_schema.exercise_names", nativeQuery = true)
    Long findMaxId();

	@Query(value="INSERT INTO gym_schema.EXERCISE_NAME (NAME,CATEGORY) VALUES (:#{#name.name},:#{#name.category})", nativeQuery = true)
	void insert(@Param("name") ExerciseName name);
	
	Long findIDByName(String name);

	ExerciseName getExerciseIdByName(String exercise);
}
