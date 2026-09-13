package com.github.dsquare68.gym.event;


import com.github.dsquare68.gym.view.CalendarWeek.ButtonDay;
import com.vaadin.flow.component.ComponentEvent;

public class CalendarSelectedDayEvent extends ComponentEvent<ButtonDay>{
	
	public CalendarSelectedDayEvent(ButtonDay source, boolean fromClient) {
		super(source, fromClient);
	}

}
