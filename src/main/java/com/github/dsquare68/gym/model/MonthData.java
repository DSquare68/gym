package com.github.dsquare68.gym.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthData {

	long numberOfTrainings;
	int totalDuration, month, year, totalReps;
	double totalWeight;
}
