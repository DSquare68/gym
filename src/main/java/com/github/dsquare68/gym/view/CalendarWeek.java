package com.github.dsquare68.gym.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import lombok.Getter;
import lombok.Setter;

import com.github.dsquare68.gym.event.*;

public class CalendarWeek extends HorizontalLayout {

	
	public class ButtonDay extends Button {
		@Getter
		private String day;
		@Getter
		@Setter
		private LocalDate date;
		public ButtonDay(String day) {
			this.day = day;	
			ButtonDay ed = this;
			this.setText(day);
			this.setClassName("calendar-day-label-no-trening");
			this.addClickListener(e -> {
				this.addClassName("calendar-day-label-trening-selected");
				ComponentUtil.fireEvent(UI.getCurrent(),new CalendarSelectedDayEvent(ed,false));
			});
		}

	}

	public CalendarWeek(ArrayList<LocalDate> dates, ArrayList<Integer> duration) {
		this.setClassName("calendar-week");
		String[] days = {"M","Tw","W","Th","F","Sa","Su"};
		Div dateLabel = new Div();
		dateLabel.setClassName("calendar-date-label");
		LocalDate first = dates.get(0);
	    int dayOfWeek = first.getDayOfWeek().getValue(); // Monday=1 ... Sunday=7
	    LocalDate dateMonday = first.minusDays(dayOfWeek - 1);
	    LocalDate dateSunday = dateMonday.plusDays(6);
	    
	    dateLabel.setText(dateMonday.getDayOfMonth() + "." + dateMonday.getMonthValue()
        + "-" + dateSunday.getDayOfMonth() + "." + dateSunday.getMonthValue());
	    this.add(dateLabel);
		for(String day: days) {
			ButtonDay dayLabel = new ButtonDay(day);
			this.add(dayLabel);
		}
		for(LocalDate date: dates) {
			int d = date.getDayOfWeek().getValue() - 1;
			this.getChildren().toList().get(1+d).setClassName("calendar-day-label-trening");
			((ButtonDay) this.getChildren().toList().get(1+d)).setDate(date);
		}
		int sum = duration.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
		Div durationLabel = new Div(sum/60+"h "+sum%60+"m");
		durationLabel.setClassName("calendar-duration-label");
		this.add(durationLabel);
	}
}
