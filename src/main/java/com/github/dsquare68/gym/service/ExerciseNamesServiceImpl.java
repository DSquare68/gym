package com.github.dsquare68.gym.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.dsquare68.gym.entity.ExerciseName;
import com.github.dsquare68.gym.repository.ExerciseNameRepository;

@Service
public class ExerciseNamesServiceImpl{

	@Autowired
	public ExerciseNameRepository repo;
	public void insertAll(ArrayList<ExerciseName> names) {
		repo.saveAll(names);
		//names.forEach(e->repo.insert(e));
	}
	public ArrayList<ExerciseName> getAllExerciseNames() {
		return (ArrayList<ExerciseName>) repo.findAll();
	}
	public Long getExerciseIdByName(String exercise) {
		return repo.getExerciseIdByName(exercise).getId();
	}
}
