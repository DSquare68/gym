package com.github.dsquare68.gym.model;

import java.util.ArrayList;

import com.github.dsquare68.gym.entity.TrainingRecord;

import lombok.Data;

@Data
public class ExerciseDetailsData {

	private ArrayList<TrainingRecord> recordsForWeight;
	double weight;
	int allCount,totalCount;
	int[] reps;
	int[] repsCount;
	double[] percentRepsInWeight;
	double[] percentOfRepsInTotal;
	double percentOfWeight;
	
	public ExerciseDetailsData(double weight,ArrayList<TrainingRecord> recordsForWeight,int totalCount) {
		this.weight = weight;
		this.recordsForWeight = recordsForWeight;
		this.totalCount = totalCount;
		calculateStats();
	}
	
	public void calculateStats() {
		this.allCount = recordsForWeight.size();
		this.reps = recordsForWeight.stream().mapToInt(TrainingRecord::getREPEAT).distinct().toArray();
		this.repsCount = new int[reps.length];
		this.percentRepsInWeight = new double[reps.length];
		this.percentOfRepsInTotal = new double[reps.length];
		for(int i=0;i<reps.length;i++) {
			int rep = reps[i];
			int count = (int) recordsForWeight.stream().filter(e->e.getREPEAT()==rep).count();
			repsCount[i] = count;
			percentRepsInWeight[i] = ((double)count/(double)allCount)*100.0;
			percentOfRepsInTotal[i] = ((double)count/(double)totalCount)*100.0;
		}
		this.percentOfWeight = ((double)allCount/(double)totalCount)*100.0;
	}

}
