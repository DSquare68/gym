package com.github.dsquare68.gym.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.github.dsquare68.gym.entity.TrainingRecord;

public interface TrainingRepository extends JpaRepository<TrainingRecord, Long> {

    /** This user's reusable schemes, newest first. */
    //List<TrainingRecord> findByUserIdAndTemplateTrueOrderByIdDesc(UUID userId);

    /** This user's performed sessions, newest first. */
    //List<TrainingRecord> findByUserIdAndTemplateFalseOrderByPerformedOnDescIdDesc(UUID userId);

    /** This user's performed sessions built from one scheme, newest first. */
    //List<TrainingRecord> findByUserIdAndTemplateFalseAndScheme_IdOrderByPerformedOnDescIdDesc(
            //UUID userId, Long schemeId);
	@Query(value = "SELECT MAX(ID_TRAINING) FROM gym_schema.TRAININGS", nativeQuery = true)
	int getMaxIDTrainingRecord();

	@Query(value = "SELECT MAX(SCHEMA) FROM gym_schema.TRAININGS", nativeQuery = true)
	int getMaxIDSchema();

	@Query(value = "SELECT * FROM gym_schema.TRAININGS where IS_SCHEMA=1 order by ID asc", nativeQuery = true)
	ArrayList<TrainingRecord> getAllSchemas();

	@Query(value = "SELECT * FROM gym_schema.TRAININGS where SCHEMA=?1 and IS_SCHEMA <> 1  order by DATE_TRAINING desc ,ID  asc", nativeQuery = true)
	ArrayList<TrainingRecord> getTrainingsPerSchema(int id);

	@Query(value = "SELECT * FROM gym_schema.TRAININGS where ID_EXERCISE_NAME=?1 and IS_SCHEMA <> 1  order by DATE_TRAINING desc ,ID  asc", nativeQuery = true)
	ArrayList<TrainingRecord> getTrainingsWithExercise(int id);

	@Query(value = "SELECT DISTINCT EXTRACT(YEAR FROM DATE_TRAINING) FROM gym_schema.TRAININGS where IS_SCHEMA <> 1 order by EXTRACT(YEAR FROM DATE_TRAINING) desc", nativeQuery = true)
	String[] getYearsWithTrainings();

	@Query(value = "SELECT DISTINCT EXTRACT(MONTH FROM DATE_TRAINING) FROM gym_schema.TRAININGS where IS_SCHEMA <> 1 order by EXTRACT(MONTH FROM DATE_TRAINING) desc", nativeQuery = true)
	String[] getMountsWithTrainings();

	@Query(value = "SELECT * FROM gym_schema.TRAININGS where EXTRACT(MONTH FROM DATE_TRAINING)=?1 and IS_SCHEMA <> 1 order by DATE_TRAINING desc ,ID  asc", nativeQuery = true)
	ArrayList<TrainingRecord> getTrainingsByMount(int mount);

	@Query(value = "SELECT * FROM gym_schema.TRAININGS where EXTRACT(YEAR FROM DATE_TRAINING)=?1 and IS_SCHEMA <> 1 order by DATE_TRAINING desc ,ID  asc", nativeQuery = true)
	ArrayList<TrainingRecord> getTrainingsByYear(int year);

	@Query(value = "SELECT * FROM gym_schema.TRAININGS where EXTRACT(YEAR FROM DATE_TRAINING)=?1 and EXTRACT(MONTH FROM DATE_TRAINING)=?2 and IS_SCHEMA <> 1 order by DATE_TRAINING desc ,ID  asc", nativeQuery = true)
	ArrayList<TrainingRecord> getTrainingsByYearAndMount(int year, int mount);

	@Query(value = "SELECT * FROM gym_schema.TRAININGS where DATE_TRAINING>=?1 and DATE_TRAINING<?2  and IS_SCHEMA <> 1 order by DATE_TRAINING desc ,ID  asc", nativeQuery = true)
	ArrayList<TrainingRecord> getTrainingsByDay(String from, String to);

	@Query(value = "SELECT DISTINCT NAME_SCHEMA FROM gym_schema.TRAININGS where IS_SCHEMA=1", nativeQuery = true)
	Set<String> getSchemaNames();

	/**
	 * Persists seed schemes, skipping any whose {@code NAME_SCHEMA} is already
	 * stored as a template, so re-running never duplicates or overwrites a scheme
	 * the user edited. Rows are grouped into schemes by name (file order kept) and
	 * each new scheme gets the next free {@code ID_TRAINING} and {@code SCHEMA}.
	 *
	 * <p>Numbers are derived in Java from the stored rows because the
	 * {@code MAX(...)} queries above return null on an empty table.
	 *
	 * @return how many schemes were actually inserted
	 */
	default int saveMissingSchemes(List<TrainingRecord> candidates) {
		if (candidates.isEmpty()) {
			return 0;
		}
		Set<String> present = getSchemaNames();
		List<TrainingRecord> stored = findAll();
		long nextTraining = stored.stream().map(TrainingRecord::getID_TRAINING)
				.filter(java.util.Objects::nonNull).mapToLong(Long::longValue).max().orElse(0) + 1;
		int nextSchema = stored.stream().mapToInt(TrainingRecord::getSCHEMA).max().orElse(0) + 1;

		Map<String, List<TrainingRecord>> byScheme = new LinkedHashMap<>();
		for (TrainingRecord r : candidates) {
			if (!present.contains(r.getNAME_SCHEMA())) {
				byScheme.computeIfAbsent(r.getNAME_SCHEMA(), k -> new ArrayList<>()).add(r);
			}
		}
		for (List<TrainingRecord> scheme : byScheme.values()) {
			for (TrainingRecord r : scheme) {
				r.setID_TRAINING(nextTraining);
				r.setSCHEMA(nextSchema);
			}
			saveAll(scheme);
			nextTraining++;
			nextSchema++;
		}
		return byScheme.size();
	}
}
