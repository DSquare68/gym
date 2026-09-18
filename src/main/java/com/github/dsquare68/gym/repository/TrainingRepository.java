package com.github.dsquare68.gym.repository;

import java.util.ArrayList;
import java.util.List;
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
}
