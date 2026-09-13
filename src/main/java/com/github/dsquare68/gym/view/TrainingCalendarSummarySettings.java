package com.github.dsquare68.gym.view;

import java.util.ArrayList;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import lombok.Getter;

import com.github.dsquare68.gym.event.*;

public class TrainingCalendarSummarySettings extends HorizontalLayout {
	@Getter
	private int year;
	@Getter
	private int mount;
	public TrainingCalendarSummarySettings(String [] years, String [] mounths) {
		this.setId("training-calendar-summary-settigns");
		Button show = new Button("Pokaż");
		ComboBox comboBoxYear = new ComboBox();
		ComboBox comboBoxMounth= new ComboBox();
		Div trainings = new Div("Treningi");
		comboBoxYear.setItems(years);
		comboBoxMounth.setItems(mounths);
		comboBoxYear.setClassName("combo-box-TCS-settings");
		comboBoxMounth.setClassName("combo-box-TCS-settings");
		trainings.setId("trainings-label-TCS-settings");
		show.setId("training-button-TCS-settings");
		comboBoxYear.addValueChangeListener(setTrainingCalendarLisener());
		comboBoxMounth.addValueChangeListener(setTrainingCalendarLisener());
		add(trainings,comboBoxMounth, comboBoxYear, show);
	}
	private ValueChangeListener setTrainingCalendarLisener() {
		TrainingCalendarSummarySettings ed = this;
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent event) {
				if(event.getHasValue().isEmpty())
					return;
				if(event.getValue().toString().length()>3)
					year = Integer.parseInt(event.getValue().toString());
				else
					mount = Integer.parseInt(event.getValue().toString());
				ComponentUtil.fireEvent(UI.getCurrent(),new TrainingCalendarSummaryEvent(ed,false));
			}
		};
	}
}
