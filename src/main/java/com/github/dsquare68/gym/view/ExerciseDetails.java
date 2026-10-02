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
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextAlignment;

public class ExerciseDetails extends HorizontalLayout {

	public ExerciseDetails(ArrayList<TrainingRecord> trainingsWithExercise) {
		this.setWidthFull();
		this.setHeight("400px");
		this.setJustifyContentMode(JustifyContentMode.AROUND);
		this.addClassNames(Padding.Top.XLARGE, TextAlignment.CENTER, Background.PRIMARY_10);
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
			this.weight = data.getWeight();
			this.reps = data.getReps();
			this.setHeight("400px");
			this.setWidth(315*(reps.length==1?2:reps.length)+"px");
			DecimalFormat df = new DecimalFormat("#0.00");
			Span weightSpan = new Span(df.format(weight)+" kg");
			weightSpan.addClassNames(GymStyles.DETAILS_VALUE);
			Span percentSpan = new Span(df.format(data.getPercentOfWeight()));
			percentSpan.addClassNames(GymStyles.DETAILS_VALUE);
			Span percentTextSpan = new Span(" % of total\n");
			percentTextSpan.addClassNames(GymStyles.DETAILS_TEXT);
			Span lineBreak = new Span(" | ");
			lineBreak.addClassNames(GymStyles.DETAILS_TEXT);
			Div weightDiv = new Div();
			weightDiv.setWidth("600px");
			weightDiv.addClassNames(Margin.AUTO);
			weightDiv.add(weightSpan,lineBreak,percentSpan,percentTextSpan);
			Div[] repsDivs = new Div[reps.length];
			for(int i=0;i<reps.length;i++) {
				Span repSpan = new Span(reps[i]+"");
				repSpan.addClassNames(GymStyles.DETAILS_VALUE);
				Span repTextSpan = new Span(" reps ");
				repTextSpan.addClassNames(GymStyles.DETAILS_TEXT);
				Span lpCount = new Span(data.getRepsCount()[i]+"");
				lpCount.addClassNames(GymStyles.DETAILS_VALUE);
				Span lpCountSpan = new Span(" LP\n");
				lpCountSpan.addClassNames(GymStyles.DETAILS_TEXT);
				Span weightPercentSpan = new Span(df.format(data.getPercentRepsInWeight()[i]));
				weightPercentSpan.addClassNames(GymStyles.DETAILS_VALUE);
				Span percentInWeightSpan = new Span(" % in weight\n");
				percentInWeightSpan.addClassNames(GymStyles.DETAILS_TEXT);
				Span totalPercentSpan = new Span(df.format(data.getPercentOfRepsInTotal()[i]));
				totalPercentSpan.addClassNames(GymStyles.DETAILS_VALUE);
				Span percentInTotalSpan = new Span(" % of total");
				percentInTotalSpan.addClassNames(GymStyles.DETAILS_TEXT);
				repsDivs[i] = new Div();
				repsDivs[i].add(repSpan,repTextSpan,lpCount,lpCountSpan,weightPercentSpan,percentInWeightSpan,totalPercentSpan,percentInTotalSpan);
				repsDivs[i].setHeight(125 + "px");
				repsDivs[i].setWidth(1025 + "px");
			}
			Div repsContainer = new Div();
			repsContainer.add(repsDivs);
			repsContainer.addClassNames(Display.FLEX, FlexDirection.ROW, JustifyContent.AROUND, AlignItems.CENTER, TextAlignment.CENTER);
			add(weightDiv,repsContainer);
			
		}
	}
}
