package com.github.dsquare68.gym.event;

import com.github.dsquare68.gym.view.ExerciseDetailsSettings;
import com.vaadin.flow.component.ComponentEvent;

public class ExerciseDetailsEvent extends ComponentEvent<ExerciseDetailsSettings>{
	
	public ExerciseDetailsEvent(ExerciseDetailsSettings source, boolean fromClient) {
		super(source, fromClient);
	}

}
