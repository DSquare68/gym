package com.github.dsquare68.gym.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.dsquare68.gym.event.*;

import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.github.dsquare68.gym.entity.ExerciseName;
import com.github.dsquare68.gym.model.Training;
import com.github.dsquare68.gym.service.ExerciseNamesServiceImpl;
import com.github.dsquare68.gym.service.TrainingServiceImpl;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;

import lombok.Getter;

@Getter
public class GymTitle extends HorizontalLayout{

	public Training schema,selectedTraining,previousTraining;
	private Div titlePage;
	private ComboBox schemas;
	private ComboBox trainingsPerSchemaComboBox;
	private ComboBox exerciseComboBox;
	private Div trainingName;
	private ArrayList<String> trainingData;
	private ArrayList<Training> trainingsPerSchema= new ArrayList<>();
	private ArrayList<ExerciseName> exerciseNames;
	private HashMap<String,Training> schemasMap = new HashMap<>();
	private HashMap<String,Training> trainingMap = new HashMap<>();
	public GymTitle(ArrayList<Training> trainingSchemas) {
		this.setId("title-gym-hl");
		this.setWidthFull();
		this.setHeight("200px");
		this.addClassNames(GymStyles.HEADER_BAR);
		this.setJustifyContentMode(JustifyContentMode.CENTER);
		this.setAlignItems(Alignment.CENTER);
		UI ui = UI.getCurrent();
		titlePage = new Div("Trening's Overview");
		trainingName = new Div("Choose training");
		for (Div div : new Div[] { titlePage, trainingName }) {
			div.setWidth("30%");
			div.addClassNames(GymStyles.HEADING);
			div.addClassNames(Display.FLEX, JustifyContent.CENTER, AlignItems.CENTER);
		}
		schemas = new ComboBox();
		trainingsPerSchemaComboBox = new ComboBox();
		for (ComboBox comboBox : new ComboBox[] { schemas, trainingsPerSchemaComboBox }) {
			comboBox.setWidth("600px");
			comboBox.addClassNames(FontSize.XXXLARGE, TextColor.SUCCESS, MaxWidth.FULL);
		}
		trainingsPerSchemaComboBox.addValueChangeListener(setTrainingView());
		ArrayList<String> schemasData = new ArrayList<>();
		if(trainingSchemas!= null) {
			trainingSchemas.forEach(e->{
				String schemaString = e.getDate()+" "+e.getName();
				schemasData.add(schemaString);
				schemasMap.put(schemaString, e);
			});
			schemas.setItems(schemasData);
		}
		VerticalLayout vl = new VerticalLayout(schemas,trainingsPerSchemaComboBox);
		vl.setId("title-gym-combobox-vl");
		vl.setWidth("40%");
		vl.addClassNames(Margin.Horizontal.AUTO);
		vl.setJustifyContentMode(JustifyContentMode.CENTER);
		vl.setAlignItems(Alignment.CENTER);
		add(titlePage,vl,trainingName);
	}
	private ValueChangeListener setTrainingView() {
		GymTitle gt = this;
		return new ValueChangeListener() {
			@Override
			public void valueChanged(ValueChangeEvent event) {
				if(event.getHasValue().isEmpty())
					return;
				selectedTraining = trainingMap.get(event.getValue().toString());
				int a= trainingData.indexOf(event.getValue().toString());
				if(a!=trainingData.size()-1)
					previousTraining = trainingMap.get(trainingData.get(a+1));
				else
					previousTraining = new Training();
				ComponentUtil.fireEvent(UI.getCurrent(),new TrainingEvent(gt,false));
			}
		};
	}
	@SuppressWarnings("unchecked")
	public void setTrainingReadPerSchema(TrainingServiceImpl trainingService, ExerciseNamesServiceImpl namesService) {
		GymTitle gt = this;
		schemas.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChanged(ValueChangeEvent event) {
				Training t = schemasMap.get(event.getValue().toString());
				schema = t;
				new Thread(() -> {
					trainingsPerSchema = trainingService.getTrainingsByIDSchema(t.getSchema());
					trainingData = new ArrayList<>();
					if(trainingsPerSchema != null)
						trainingsPerSchema.forEach(e->{
						String schemaString = e.getDate()+" "+e.getName();
						trainingData.add(schemaString);
						trainingMap.put(schemaString, e);
					});
						new Thread (getUI().get().accessLater(() -> {
							if(trainingsPerSchema==null || trainingsPerSchema.size()==0) {
								trainingData.clear();
								trainingData.add("No Trainings");
							}
							trainingsPerSchemaComboBox.setItems(trainingData);
							ComponentUtil.fireEvent(UI.getCurrent(),new SchemaEvent(gt,false));
						},null)).start();
				}).start();
			}
		});
	}
}
