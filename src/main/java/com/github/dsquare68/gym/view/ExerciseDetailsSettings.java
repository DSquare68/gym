package com.github.dsquare68.gym.view;

import java.util.ArrayList;

import com.github.dsquare68.gym.entity.ExerciseName;
import com.github.dsquare68.gym.event.*;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import lombok.Getter;


public class ExerciseDetailsSettings extends HorizontalLayout {
	@Getter
	private String exerciseName;
	private ComboBox exerciseComboBox;
	public ExerciseDetailsSettings(ArrayList<ExerciseName> exerciseNames) {
		this.setId("exercise-details-vl");
		Div title = new Div("Szczegóły ćwiczenia");
		title.setId("exercise-details-title");
		exerciseComboBox = new ComboBox();
		exerciseComboBox.setId("exercise-details-combobox");
		exerciseComboBox.setItems(exerciseNames.stream().map(ExerciseName::getName).toArray(String[]::new));
		exerciseComboBox.addValueChangeListener(setExerciseLisener());
		this.add(title,exerciseComboBox);
	}
	
	private ValueChangeListener setExerciseLisener() {
		ExerciseDetailsSettings ed = this;
		return new ValueChangeListener() {

			@Override
			public void valueChanged(ValueChangeEvent event) {
				if(event.getHasValue().isEmpty())
					return;
				exerciseName = event.getValue().toString();
				ComponentUtil.fireEvent(UI.getCurrent(),new ExerciseDetailsEvent(ed,false));
			}
		};
	}
}
