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
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;

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
			this.setWidth("60px");
			this.setHeight("60px");
			this.addClassNames(FontSize.XXLARGE);
			this.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
			this.addClickListener(e -> {
				GymStyles.setSelected(this, true);
				ComponentUtil.fireEvent(UI.getCurrent(),new CalendarSelectedDayEvent(ed,false));
			});
		}

	}

	public CalendarWeek(ArrayList<LocalDate> dates, ArrayList<Integer> duration) {
		this.setAlignItems(Alignment.CENTER);
		String[] days = {"M","Tw","W","Th","F","Sa","Su"};
		Div dateLabel = new Div();
		dateLabel.setWidth("150px");
		dateLabel.addClassNames(Margin.Left.LARGE, FontSize.XLARGE,
				Display.FLEX, JustifyContent.AROUND, AlignItems.CENTER);
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
			ButtonDay dayButton = (ButtonDay) this.getChildren().toList().get(1+d);
			dayButton.removeThemeVariants(ButtonVariant.LUMO_ERROR);
			dayButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
			dayButton.setDate(date);
		}
		int sum = duration.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).sum();
		Div durationLabel = new Div(sum/60+"h "+sum%60+"m");
		durationLabel.setWidth("150px");
		durationLabel.addClassNames(Margin.Right.LARGE, FontSize.XXLARGE,
				Display.FLEX, JustifyContent.AROUND, AlignItems.CENTER);
		this.add(durationLabel);
	}
}
