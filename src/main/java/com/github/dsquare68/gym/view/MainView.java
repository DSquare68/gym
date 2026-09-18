package com.github.dsquare68.gym.view;

import java.util.ArrayList;

import com.github.dsquare68.gym.PluginInfo;
import com.github.dsquare68.gym.entity.ExerciseName;
import com.github.dsquare68.gym.entity.TrainingRecord;
import com.github.dsquare68.gym.event.*;
import com.github.dsquare68.gym.model.Training;
import com.github.dsquare68.gym.persistence.GymPersistence;
import com.github.dsquare68.gym.service.ExerciseNamesServiceImpl;
import com.github.dsquare68.gym.service.TrainingServiceImpl;
import com.github.dsquare68.homeforgeapi.ui.BaseLayout;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import jakarta.annotation.security.PermitAll;

@PageTitle(PluginInfo.TITLE)
@Route(layout = BaseLayout.class, value = "gym")
@PermitAll
public class MainView extends VerticalLayout {

	private static final long serialVersionUID = 3275850456945504655L;
	private ExerciseDetails exerciseDetails;
	private HorizontalLayout trainings;
	private Training schema, training;
	private TrainingView schemaView, trainingView;
	private GymTitle title;
	private ArrayList<Training> schemas;
	private ArrayList<ExerciseName> exerciseNames;
	private Div exerciseDetailsDiv,trainingCalendarSummaryDiv;
	private TrainingCalendarSummary trainingCalendarSummary;
	private TrainingCalendarSummarySettings trainingCalendarSummarySettings;
	
	/**
	 * No-arg on purpose: HUB registers this class directly with Vaadin's
	 * {@code RouteConfiguration}, so HUB's own Spring application context is
	 * what instantiates it on every navigation - and that context has no
	 * beans for this plugin's services. {@link GymPersistence#current()} is
	 * how the plugin reaches its own JPA-context beans instead.
	 */
	public MainView(){
		GymPersistence persistence = GymPersistence.current();
		TrainingServiceImpl trainingService = persistence.trainingService();
		ExerciseNamesServiceImpl namesService = persistence.exerciseNamesService();

		trainings = new HorizontalLayout();
		trainings.setId("trainings-hl");
		schemas = trainingService.getSchemasDataTraining();
		exerciseNames = namesService.getAllExerciseNames();
		title = new GymTitle(schemas);
		title.setTrainingReadPerSchema(trainingService,namesService);
		exerciseDetailsDiv = new Div();
		trainingCalendarSummaryDiv = new Div();
		trainingCalendarSummaryDiv.setWidth(100,Unit.PERCENTAGE);
		String[] years = trainingService.getYearsWithTrainings();
		String[] mounths = trainingService.getMountsWithTrainings();
		ComponentUtil.addListener(UI.getCurrent(),SchemaEvent.class,e->{
			this.trainings.removeAll();
			schema = e.getSource().getSchema();
			Training  lastTraining = new Training();
			schemaView = new TrainingView(schema,lastTraining);
			schemaView.setId("schema-view-vl");
			this.trainings.add(schemaView);
			schemaView.setWidth(40,Unit.PERCENTAGE);
		});
		ComponentUtil.addListener(UI.getCurrent(),TrainingEvent.class,e->{
			if(this.trainings.getChildren().filter(f->f.equals(trainingView)).findAny().isPresent())
				this.trainings.remove(trainingView);
			training = e.getSource().getSelectedTraining();
			Training  lastTraining = e.getSource().getPreviousTraining();
			trainingView = new TrainingView(training,lastTraining);
			trainingView.setId("training-view-vl");
			this.trainings.add(trainingView);
			trainingView.setWidth(40,Unit.PERCENTAGE);
		});
		ComponentUtil.addListener(UI.getCurrent(),TrainingCalendarSummaryEvent.class,e->{
			if(this.trainingCalendarSummaryDiv.getChildren().filter(f->f.equals(trainingCalendarSummary)).findAny().isPresent()) {
				this.trainingCalendarSummaryDiv.remove(trainingCalendarSummary);
			}
			int year = e.getSource().getYear();
			int mount = e.getSource().getMount();
			ArrayList<TrainingRecord> trainingsWithExercise = trainingService.getTrainingsByYearAndMount(year,mount);
			
			trainingCalendarSummary=new TrainingCalendarSummary(trainingService,trainingsWithExercise,year,mount);
			trainingCalendarSummaryDiv.add(trainingCalendarSummary);
			
		});
		//trainingCalendarSummary=new TrainingCalendarSummary(trainingService,trainingService.getAllFromMount(Calendar.MONTH+1,Calendar.YEAR),Calendar.YEAR,Calendar.MONTH+1); //TODO remove on production
		//trainingCalendarSummaryDiv.add(trainingCalendarSummary);
		ExerciseDetailsSettings exerciseDetailsSettings = new ExerciseDetailsSettings(exerciseNames);
		TrainingCalendarSummarySettings trainingCalendarSummarySettings = new TrainingCalendarSummarySettings(years,mounths);
		add(new VerticalLayout(title,exerciseDetailsSettings,trainingCalendarSummarySettings,trainings,exerciseDetailsDiv,trainingCalendarSummaryDiv));
		
	}
}
