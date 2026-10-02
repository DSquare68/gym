package com.github.dsquare68.gym.view;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import com.github.dsquare68.gym.entity.TrainingRecord;
import com.github.dsquare68.gym.event.CalendarSelectedDayEvent;
import com.github.dsquare68.gym.service.TrainingServiceImpl;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;


public class TrainingCalendarDay extends VerticalLayout {
	private LocalDate date;
	private ArrayList<TrainingRecord> trainings;
	public TrainingCalendarDay(TrainingServiceImpl trainingService) {
		this.setId("training-calendar-day");
		this.setWidthFull();
		this.setHeight("300px");
		this.addClassNames(GymStyles.CALENDAR_PANEL);
		ComponentUtil.addListener(UI.getCurrent(),CalendarSelectedDayEvent.class,e->{
			this.remove();
			date = e.getSource().getDate();
			if(date != null) { 
				trainings = trainingService.getTrainingsByDay(date);
				if(!trainings.isEmpty())
					refreashDate(trainings);
			}
			this.add();
		});
		
	}
	private void refreashDate(ArrayList<TrainingRecord> trainings2) {
		this.removeAll();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		Div numberOfTrainings = new Div("Data Treningu: "+String.valueOf(dateFormat.format(trainings2.get(0).getDATE_TRAINING())));
		numberOfTrainings.setId("training-calendar-day-date");
		Div totalDuration = new Div("Długość treniningu: "+trainings2.get(0).getTIME_TRAINING());
		totalDuration.setId("training-calendar-day-duration");
		Div totalReps = new Div("Wszystkie powtórzenia: "+String.valueOf(trainings2.stream().mapToInt(TrainingRecord::getREPEAT).sum()));
		totalReps.setId("training-calendar-day-total-reps");
		Div totalWeight = new Div(String.format("Suma obciążenia: %.1f kg", trainings2.stream().mapToDouble(e->e.getWEIGHT()).sum()));
		totalWeight.setId("training-calendar-day-total-weight");
		Div totalSets = new Div(String.format("Suma obciążenia: %.1f kg", trainings2.stream().mapToDouble(e->e.getWEIGHT()).sum()));
		totalWeight.setId("training-calendar-day-total-weight");
		for (Div row : new Div[] { numberOfTrainings, totalDuration, totalReps, totalWeight }) {
			row.setHeight("50px");
			row.addClassNames(GymStyles.CALENDAR_DATA);
			row.addClassNames(Margin.Left.XLARGE);
		}
		this.add(numberOfTrainings, totalDuration, totalReps, totalWeight);
	}
	

}
