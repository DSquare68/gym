package com.github.dsquare68.gym.event;

import com.github.dsquare68.gym.view.GymTitle;
import com.vaadin.flow.component.ComponentEvent;

public class SchemaEvent extends ComponentEvent<GymTitle> {

	public SchemaEvent(GymTitle source, boolean fromClient) {
		super(source, fromClient);
	}

}
