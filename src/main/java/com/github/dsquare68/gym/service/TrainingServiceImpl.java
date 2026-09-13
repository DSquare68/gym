package com.github.dsquare68.gym.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.dsquare68.gym.entity.TrainingRecord;
import com.github.dsquare68.gym.model.Training;
import com.github.dsquare68.gym.repository.TrainingRepository;


@Service
public class TrainingServiceImpl {

	@Autowired
	private TrainingRepository trainingRepository;
	@Autowired
	private ExerciseNamesServiceImpl exerciseService;
	
	public void addTrainingRecord(TrainingRecord record) {
		trainingRepository.save(record);
	}
	
	public void addTraining(ArrayList<TrainingRecord> record) {
		trainingRepository.saveAll(record);
	}

	public ArrayList<Training> getSchemasDataTraining() {
		return TrainingRecord.toTraininings(trainingRepository.getAllSchemas(), exerciseService);
	}

	public ArrayList<Training> getTrainingsByIDSchema(int id) {
		return TrainingRecord.toTraininings(trainingRepository.getTrainingsPerSchema(id), exerciseService);
	}

	public ArrayList<TrainingRecord> getTrainingsWithExercise(int id) {
		return trainingRepository.getTrainingsWithExercise(id);
	}

	public String[] getYearsWithTrainings() {
		return trainingRepository.getYearsWithTrainings();
	}

	public String[] getMountsWithTrainings() {
		return trainingRepository.getMountsWithTrainings();
	}

	public ArrayList<TrainingRecord> getTrainingsByYearAndMount(int year, int mount) {
		if(year==0 && mount==0) 
			return (ArrayList<TrainingRecord>) trainingRepository.findAll();
		else if(year==0) 
			return trainingRepository.getTrainingsByMount(mount);
		else if(mount==0) 
			return trainingRepository.getTrainingsByYear(year);
		else
			return trainingRepository.getTrainingsByYearAndMount(year, mount);
	}

	public ArrayList<TrainingRecord> getAll() {
		return (ArrayList<TrainingRecord>) trainingRepository.findAll();
	}

	public ArrayList<TrainingRecord> getTrainingsByDay(LocalDate date) {
		LocalDate tomorrowDay = date;
		//tomorrowDay.setDate(tomorrowDay.getDate()+1);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String todayS = formatter.format(date);
        String tomorrowS = formatter.format(tomorrowDay);
		return trainingRepository.getTrainingsByDay(todayS, tomorrowS);
	}

	public ArrayList<TrainingRecord> getAllFromMount(int i, int year) {
		return trainingRepository.getTrainingsByDay(""+year+"/"+i+"/1", ""+year+"/"+(i+1)+"/1");
	}
}
