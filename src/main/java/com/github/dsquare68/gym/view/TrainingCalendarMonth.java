package com.github.dsquare68.gym.view;

import com.github.dsquare68.gym.model.MonthData;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;

public class TrainingCalendarMonth extends VerticalLayout {

	public TrainingCalendarMonth(MonthData data) {
		this.setId("training-calendar-mounth");
		this.setWidthFull();
		this.setHeight("300px");
		this.addClassNames(GymStyles.CALENDAR_PANEL);
		Div numberOfTrainings = new Div("Liczba Treningów: "+String.valueOf(data.getNumberOfTrainings()));
		numberOfTrainings.setId("training-calendar-mounth-number-of-trainings");
		Div totalDuration = new Div("Suma długości treniningów: "+(data.getTotalDuration()/60)+"h "+(data.getTotalDuration()%60)+"m");
		totalDuration.setId("training-calendar-mounth-total-duration");
		Div totalReps = new Div("Wszystkie powtórzenia: "+String.valueOf(data.getTotalReps()));
		totalReps.setId("training-calendar-mounth-total-reps");
		Div totalWeight = new Div(String.format("Suma obciążenia: %.1f kg", data.getTotalWeight()));
		totalWeight.setId("training-calendar-mounth-total-weight");
		Div monthYear = new Div("Data: "+(data.getMonth()+1)+"."+(data.getYear()+1900));
		monthYear.setId("training-calendar-mounth-month-year");
		for (Div row : new Div[] { monthYear, numberOfTrainings, totalDuration, totalReps, totalWeight }) {
			row.setHeight("50px");
			row.addClassNames(GymStyles.CALENDAR_DATA);
			row.addClassNames(Margin.Left.XLARGE);
		}
		this.add(monthYear,numberOfTrainings, totalDuration, totalReps, totalWeight);
		
	}
}
