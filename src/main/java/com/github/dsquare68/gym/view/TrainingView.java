package com.github.dsquare68.gym.view;

import java.util.ArrayList;
import java.util.HashMap;

import com.github.dsquare68.gym.model.Training;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class TrainingView extends VerticalLayout{
	ArrayList<Exercise> exercise;
	private String[] emptyName = {"a","b","c","d","e","f","g","h","j","k","l"};
	public TrainingView(Training training, Training lastTraining) {
		this.setClassName("training-view-vl");
		Div nameTraining = new Div(training.getName());
		nameTraining.setClassName("name-training-div");
		add(nameTraining);
		exercise = new ArrayList<Exercise>();
		String nameLastTraining;
		int k=0;
		for(String name : training.getExercises()) {
			if(lastTraining.getName().equals("Empty"))
				nameLastTraining = emptyName[k++];
			else 
				nameLastTraining = name;
			exercise.add(new Exercise(name,training.getRounds().get(name),lastTraining.getRounds().get(nameLastTraining)));
			this.add(exercise.get(exercise.size()-1));
		}
	}
	private class Exercise extends VerticalLayout{
		HorizontalLayout hl;
		
		public Exercise(String name,ArrayList<Training.Round> rounds, ArrayList<Training.Round> lastRounds) {
			this.addClassName("exercise-hl");
			hl = new HorizontalLayout();
			Div nameT = new Div(name);
			nameT.setClassName("name-exercise-div");
			for(int i=0;i<rounds.size();i++) {
				if(i>=lastRounds.size())
					hl.add(new Round(rounds.get(i),new Training().new Round(0,0,0)));
				else
					hl.add(new Round(rounds.get(i),lastRounds.get(i)));
			}
			this.add(nameT,hl);
		}
	}
	private class Round extends HorizontalLayout{
		public Training.Round round,lastRound;
		
		public Round(Training.Round round, Training.Round lastRound) {
			this.round= round;
			this.lastRound= lastRound;
			this.setClassName("round-vl");
			Div lp = new Div(round.getRoundNumber()+"");
			lp.setClassName("lp-div");
			VerticalLayout data = setDataRound();
			this.add(lp,data);
		}

		private VerticalLayout setDataRound() {
			VerticalLayout data = new VerticalLayout();
			data.setClassName("data-round-vl");
			Div weight = new Div(round.getWeight()+"");
			if (round.getWeight()==lastRound.getWeight()||lastRound.getWeight()==0)
				weight.addClassName("equal-data-div");
			else if(round.getWeight()<lastRound.getWeight())
				weight.addClassName("worse-data-div");
			else if (round.getWeight()>lastRound.getWeight()) 
				weight.addClassName("better-data-div");
			Div repeats = new Div(round.getReps()+"");
			if(round.getReps()==lastRound.getReps()||lastRound.getReps()==0)
				repeats.addClassName("equal-data-div");
			else if(round.getReps()<lastRound.getReps())
				repeats.addClassName("worse-data-div");
			else if(round.getReps()>lastRound.getReps())
				repeats.addClassName("better-data-div");
			data.add(weight,repeats);
			return data;
		}
		
	}
}
