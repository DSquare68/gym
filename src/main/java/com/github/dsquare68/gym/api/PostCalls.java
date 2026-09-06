package com.github.dsquare68.gym.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.dsquare68.gym.entity.ExerciseName;
import com.github.dsquare68.gym.entity.TrainingRecord;
import com.github.dsquare68.gym.repository.ExerciseNameRepository;
import com.github.dsquare68.gym.repository.TrainingRepository;

@RestController
public class PostCalls {

	@Autowired
	private ExerciseNameRepository namesRepository;
	
	@Autowired
	private TrainingRepository trainingRepository;
	
	@PostMapping("/api/add/exercise_name")
	public ResponseEntity<ExerciseName> addExercise(@RequestBody ExerciseName e) {
		namesRepository.save(e);
		return ResponseEntity.status(200).build();
	}

	@PostMapping("/api/add/exercise_names")
	public ResponseEntity<ExerciseName> addExercises(@RequestBody List<ExerciseName> es) {
		es = es.stream().filter(e -> e != null || e.getName() != null).collect(java.util.stream.Collectors.toList());
		for (ExerciseName e : es)
			namesRepository.save(e);
		return ResponseEntity.status(200).build();
	}

	@PostMapping("/api/add/training_record") // ?type=class
	public ResponseEntity<TrainingRecord> addTrainingRecord(@RequestBody TrainingRecord t) {
		trainingRepository.save(t);
		return ResponseEntity.status(200).build();
	}

	/*
	 * @PostMapping("/api/add/training_record")//type=json public
	 * ResponseEntity<TrainingRecord> addTrainingRecord(@RequestBody String t) { int
	 * a=0; return ResponseEntity.status(200).build(); }
	 */
	@PostMapping("/api/add/training")
	public ResponseEntity<TrainingRecord> addTraining(@RequestBody ArrayList<TrainingRecord> ts) {
		ts = (ArrayList<TrainingRecord>) ts.stream().filter(e -> e != null)
				.collect(java.util.stream.Collectors.toList());
		long ID_TRAINING = trainingRepository.getMaxIDTrainingRecord() + 1;
		int ID_SCHEMA = ts.get(0).getScheme(); // Assuming all records have the same schema
		if (ts.get(0).getIS_SCHEMA() == 1)
			ID_SCHEMA = trainingRepository.getMaxIDSchema() + 1;
		for (TrainingRecord e : ts) {
			e.setId(ID_TRAINING);
			e.setScheme(ID_SCHEMA);
			trainingRepository.save(e);
		}
		return ResponseEntity.status(200).build();
	}
	

}
