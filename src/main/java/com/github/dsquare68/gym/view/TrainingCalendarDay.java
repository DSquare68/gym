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


public class TrainingCalendarDay extends VerticalLayout {
	private LocalDate date;
	private ArrayList<TrainingRecord> trainings;
	public TrainingCalendarDay(TrainingServiceImpl trainingService) {
		this.setId("training-calendar-day");
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
		numberOfTrainings.setClassName("training-calendar-day-data");
		Div totalDuration = new Div("Długość treniningu: "+trainings2.get(0).getTIME_TRAINING());
		totalDuration.setId("training-calendar-day-duration");
		totalDuration.setClassName("training-calendar-day-data");
		Div totalReps = new Div("Wszystkie powtórzenia: "+String.valueOf(trainings2.stream().mapToInt(TrainingRecord::getREPEAT).sum()));
		totalReps.setId("training-calendar-day-total-reps");
		totalReps.setClassName("training-calendar-day-data");
		Div totalWeight = new Div(String.format("Suma obciążenia: %.1f kg", trainings2.stream().mapToDouble(e->e.getWEIGHT()).sum()));
		totalWeight.setId("training-calendar-day-total-weight");
		Div totalSets = new Div(String.format("Suma obciążenia: %.1f kg", trainings2.stream().mapToDouble(e->e.getWEIGHT()).sum()));
		totalWeight.setId("training-calendar-day-total-weight");
		totalWeight.setClassName("training-calendar-day-data");
		this.add(numberOfTrainings, totalDuration, totalReps, totalWeight);
	}
	

}
