package com.github.dsquare68.gym.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.github.dsquare68.gym.entity.ExerciseName;
import com.github.dsquare68.gym.entity.TrainingRecord;
import com.github.dsquare68.gym.repository.ExerciseNameRepository;
import com.github.dsquare68.gym.repository.TrainingRepository;

public class GetCalls {
	
	@Autowired
	private ExerciseNameRepository exerciseNameRepository;
	@Autowired
	private TrainingRepository trainingRepository;
	
	@GetMapping("/exercise_name/{id}")
	public ExerciseName getExerciseName(@PathVariable("id") int id) {
		return exerciseNameRepository.findById((long) id).orElse(null);
		}
	@GetMapping("/exercises")
	public List<ExerciseName> getExercises() {
		return exerciseNameRepository.findAll();
	}
	@GetMapping("/TrainingSchemas")
	public ArrayList<TrainingRecord> getTrainingSchemas() {
		ArrayList<TrainingRecord> tr =  trainingRepository.getAllSchemas();
		return tr;
	}
}
