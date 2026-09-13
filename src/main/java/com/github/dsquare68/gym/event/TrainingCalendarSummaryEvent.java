package com.github.dsquare68.gym.event;

import com.github.dsquare68.gym.view.TrainingCalendarSummarySettings;
import com.vaadin.flow.component.ComponentEvent;

public class TrainingCalendarSummaryEvent extends ComponentEvent<TrainingCalendarSummarySettings>{

		public TrainingCalendarSummaryEvent(TrainingCalendarSummarySettings source, boolean fromClient) {
			super(source, fromClient);
		}
	}

