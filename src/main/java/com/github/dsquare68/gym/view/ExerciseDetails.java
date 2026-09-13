package com.github.dsquare68.gym.view;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.github.dsquare68.gym.entity.TrainingRecord;
import com.github.dsquare68.gym.model.ExerciseDetailsData;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class ExerciseDetails extends HorizontalLayout {

	public ExerciseDetails(ArrayList<TrainingRecord> trainingsWithExercise) {
		this.setClassName("exercise-details-layout");
		int allCount = trainingsWithExercise.size();
		Object[] weights = trainingsWithExercise.stream()
								.mapToDouble(TrainingRecord::getWEIGHT)
								.distinct()
								.boxed()
								.toArray();
		weights = Arrays.stream(weights).sorted((a,b)->Double.compare((double)b,(double)a)).toArray();
		ExerciseDetailsData[] data= new ExerciseDetailsData[weights.length];
		int k=0;
		for(Object w:weights) {
			data[k++] = new ExerciseDetailsData((double)w,trainingsWithExercise.stream()
													.filter(e->e.getWEIGHT()==(double)w)
													.collect(Collectors.toCollection(ArrayList::new)),
												allCount);
		}
		for(int i=0;i<weights.length;i++) {
			this.add(new WeightSet(data[i]));
		}
	}
	class WeightSet extends Div{
		Double weight;
		int[] reps;
		
		public WeightSet(ExerciseDetailsData data) {
			this.setClassName("exercise-details-weight-set");
			this.weight = data.getWeight();
			this.reps = data.getReps();
			this.setHeight("400px");
			this.setWidth(315*(reps.length==1?2:reps.length)+"px");
			DecimalFormat df = new DecimalFormat("#0.00");
			Span weightSpan = new Span(df.format(weight)+" kg");
			weightSpan.addClassName("exercise-details-span-blue");
			weightSpan.addClassName("exercise-details-span-weight");
			Span percentSpan = new Span(df.format(data.getPercentOfWeight()));
			percentSpan.addClassName("exercise-details-span-blue");
			percentSpan.addClassName("exercise-details-span-weight");
			Span percentTextSpan = new Span(" % of total\n");
			percentTextSpan.addClassName("exercise-details-span-white");
			percentTextSpan.addClassName("exercise-details-span-weight");
			Span lineBreak = new Span(" | ");
			lineBreak.addClassName("exercise-details-span-white");
			lineBreak.addClassName("exercise-details-span-weight");
			Div weightDiv = new Div();
			weightDiv.setClassName("exercise-details-weight-div");
			weightDiv.add(weightSpan,lineBreak,percentSpan,percentTextSpan);
			Div[] repsDivs = new Div[reps.length];
			for(int i=0;i<reps.length;i++) {
				Span repSpan = new Span(reps[i]+"");
				repSpan.addClassName("exercise-details-span-blue");
				repSpan.addClassName("exercise-details-span-reps");
				Span repTextSpan = new Span(" reps ");
				repTextSpan.addClassName("exercise-details-span-white");
				repTextSpan.addClassName("exercise-details-span-reps");
				Span lpCount = new Span(data.getRepsCount()[i]+"");
				lpCount.addClassName("exercise-details-span-blue");
				lpCount.addClassName("exercise-details-span-reps");
				Span lpCountSpan = new Span(" LP\n");
				lpCountSpan.addClassName("exercise-details-span-white");
				lpCountSpan.addClassName("exercise-details-span-reps");
				Span weightPercentSpan = new Span(df.format(data.getPercentRepsInWeight()[i]));
				weightPercentSpan.addClassName("exercise-details-span-blue");
				weightPercentSpan.addClassName("exercise-details-span-reps");
				Span percentInWeightSpan = new Span(" % in weight\n");
				percentInWeightSpan.addClassName("exercise-details-span-white");
				percentInWeightSpan.addClassName("exercise-details-span-reps");
				Span totalPercentSpan = new Span(df.format(data.getPercentOfRepsInTotal()[i]));
				totalPercentSpan.addClassName("exercise-details-span-blue");
				totalPercentSpan.addClassName("exercise-details-span-reps");
				Span percentInTotalSpan = new Span(" % of total");
				percentInTotalSpan.setClassName("exercise-details-span-white");
				percentInTotalSpan.setClassName("exercise-details-span-reps");
				repsDivs[i] = new Div();
				repsDivs[i].add(repSpan,repTextSpan,lpCount,lpCountSpan,weightPercentSpan,percentInWeightSpan,totalPercentSpan,percentInTotalSpan);
				repsDivs[i].setHeight(125 + "px");
				repsDivs[i].setWidth(1025 + "px");
				repsDivs[i].setClassName("exercise-details-reps-div");
			}
			Div repsContainer = new Div();
			repsContainer.add(repsDivs);
			repsContainer.setClassName("exercise-details-reps-container");
			add(weightDiv,repsContainer);
			
		}
	}
}
