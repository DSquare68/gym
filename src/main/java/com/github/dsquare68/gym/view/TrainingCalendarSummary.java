package com.github.dsquare68.gym.view;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.dsquare68.gym.entity.TrainingRecord;
import com.github.dsquare68.gym.event.CalendarSelectedDayEvent;
import com.github.dsquare68.gym.model.MonthData;
import com.github.dsquare68.gym.service.TrainingServiceImpl;
import com.github.dsquare68.gym.view.CalendarWeek.ButtonDay;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class TrainingCalendarSummary extends HorizontalLayout {

	private ButtonDay selectedDay;
	
	public TrainingCalendarSummary(TrainingServiceImpl trainigService,ArrayList<TrainingRecord> trainings, int year, int mount) {
		this.setId("training-calendar-summary");
		VerticalLayout calendarWeeks = new VerticalLayout();
		calendarWeeks.setId("calendar-weeks");
		trainings.stream().sorted((e,r) -> e.getDATE_TRAINING().compareTo(r.getDATE_TRAINING()));
		ArrayList<ArrayList<LocalDate>> weeks = new ArrayList<>();
		//Map<Date, ArrayList<Date>> weeksMap = new LinkedHashMap<>();
		//ArrayList<Date> weeksMap = new ArrayList<>();
		//ArrayList<ArrayList<Integer>> durationMap =  new ArrayList<>();
		ArrayList<ArrayList<Integer>> durations = new ArrayList<>();
		int ID_training = 0;
		LocalDate prevWeek = null;
		int lp =-1;
		for (TrainingRecord record : trainings.stream().collect(Collectors.toMap(TrainingRecord::getDATE_TRAINING,Function.identity(),(e,r)->e)).values().stream().collect(Collectors.toList()).stream().sorted((e,r) -> e.getDATE_TRAINING().compareTo(r.getDATE_TRAINING())).collect(Collectors.toList())) {	
			LocalDate date = record.getDATE_TRAINING();
			LocalDate weekStart = startOfWeekMonday(date);
			//weeksMap.computeIfAbsent(weekStart, k -> new ArrayList<>()).add(date);
			if(!weekStart.equals(prevWeek)) {
				lp++;
				prevWeek = weekStart;
				weeks.add(new ArrayList<>());
				durations.add(new ArrayList<>());
			}
			weeks.get(lp).add(date);
			durations.get(lp).add(timeToInt(record.getTIME_TRAINING()));
			//if(ID_training != record.getID_TRAINING()) {
				//ID_training = record.getID_TRAINING();
			//	durationMap.computeIfAbsent(weekStart, k -> new ArrayList<>()).add(timeToInt(record.getTIME_TRAINING()));
			//}
		}
		//weeks = new ArrayList<>(weeksMap.values());
		//durations = new ArrayList<>(durationMap.values());
		for(int i=0; i<weeks.size(); i++) {
			calendarWeeks.add(new CalendarWeek(weeks.get(i),durations.get(i)));
		}
		calendarWeeks.setHeight(calendarWeeks.getChildren().count()*75+"px");
		calendarWeeks.setId("calendar-weeks");
		calendarWeeks.setWidth("50%");
		VerticalLayout dayAndMonth = new VerticalLayout();
		dayAndMonth.setId("day-and-month");
		dayAndMonth.setWidth("50%");
		if(!trainings.isEmpty()) 
			dayAndMonth.add(new TrainingCalendarMonth(new MonthData(
				trainings.stream().map(e -> e.getID_TRAINING()).distinct().count(),
				trainings.stream().mapToInt(TrainingRecord::getREPEAT).sum(),
				mount,
				year,
				trainings.stream().collect(Collectors.toMap(TrainingRecord::getID_TRAINING, TrainingRecord::getTIME_TRAINING,(e,r)->e)).values().stream().map(e->timeToInt(e.toString())).mapToInt(e->e).sum(),
				(double)trainings.stream().mapToDouble(e->e.getWEIGHT()).sum())));
		else 
			dayAndMonth.add(new TrainingCalendarMonth(new MonthData(0,0,0,0,0,0)));
		dayAndMonth.add(new TrainingCalendarDay(trainigService));
		this.add(calendarWeeks,dayAndMonth);
		ComponentUtil.addListener(UI.getCurrent(),CalendarSelectedDayEvent.class,e->{
			if(selectedDay != null) 
				selectedDay.removeClassName("calendar-day-label-trening-selected");
			selectedDay = e.getSource();
			
	});
	}
	private Integer timeToInt(String time_TRAINING) {
		if (time_TRAINING == null) return 0;
		String s = time_TRAINING.trim().toLowerCase();
		try {
			// HH:mm or H:mm or HH:mm:ss
			Pattern p1 = Pattern.compile("^(\\d{1,2}):(\\d{2})(?::\\d{2})?$");
			Matcher m1 = p1.matcher(s);
			if (m1.matches()) {
				int h = Integer.parseInt(m1.group(1));
				int min = Integer.parseInt(m1.group(2));
				return h * 60 + min;
			}
		}catch (NumberFormatException e) {
			System.out.println("Invalid time format: " + time_TRAINING);
		}
		return 0;
	}
	
	private LocalDate startOfWeekMonday(LocalDate date) {
		int dow = date.getDayOfWeek().getValue();
		int diff = dow - DayOfWeek.MONDAY.getValue();
		if(diff<0) diff+=7;
		date.minusDays(diff);
		return date;
	}
}
