package com.github.dsquare68.gym.view;

import com.github.dsquare68.gym.model.MonthData;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class TrainingCalendarMonth extends VerticalLayout {

	public TrainingCalendarMonth(MonthData data) {
		this.setId("training-calendar-mounth");
		Div numberOfTrainings = new Div("Liczba Treningów: "+String.valueOf(data.getNumberOfTrainings()));
		numberOfTrainings.setId("training-calendar-mounth-number-of-trainings");
		numberOfTrainings.setClassName("training-calendar-mounth-data");
		Div totalDuration = new Div("Suma długości treniningów: "+(data.getTotalDuration()/60)+"h "+(data.getTotalDuration()%60)+"m");
		totalDuration.setId("training-calendar-mounth-total-duration");
		totalDuration.setClassName("training-calendar-mounth-data");
		Div totalReps = new Div("Wszystkie powtórzenia: "+String.valueOf(data.getTotalReps()));
		totalReps.setId("training-calendar-mounth-total-reps");
		totalReps.setClassName("training-calendar-mounth-data");
		Div totalWeight = new Div(String.format("Suma obciążenia: %.1f kg", data.getTotalWeight()));
		totalWeight.setId("training-calendar-mounth-total-weight");
		totalWeight.setClassName("training-calendar-mounth-data");
		Div monthYear = new Div("Data: "+(data.getMonth()+1)+"."+(data.getYear()+1900));
		monthYear.setId("training-calendar-mounth-month-year");
		monthYear.setClassName("training-calendar-mounth-data");
		this.add(monthYear,numberOfTrainings, totalDuration, totalReps, totalWeight);
		
	}
}
