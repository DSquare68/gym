package com.github.dsquare68.gym.view;

import java.util.ArrayList;
import java.util.HashMap;

import com.github.dsquare68.gym.model.Training;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignSelf;
import com.vaadin.flow.theme.lumo.LumoUtility.Background;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;

public class TrainingView extends VerticalLayout{
	ArrayList<Exercise> exercise;
	private String[] emptyName = {"a","b","c","d","e","f","g","h","j","k","l"};
	public TrainingView(Training training, Training lastTraining) {
		this.setWidth("40%");
		this.setHeight("1000px");
		this.addClassNames(Background.SUCCESS_10);
		Div nameTraining = new Div(training.getName());
		nameTraining.addClassNames(AlignSelf.CENTER, FontSize.XXXLARGE, FontWeight.BOLD, TextColor.PRIMARY);
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
			this.setHeight("150px");
			hl = new HorizontalLayout();
			Div nameT = new Div(name);
			nameT.addClassNames(FontSize.XLARGE, Margin.Left.XSMALL, FontWeight.NORMAL);
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
			this.setWidth("75px");
			this.setHeight("75px");
			this.addClassNames(Margin.Right.XLARGE);
			Div lp = new Div(round.getRoundNumber()+"");
			lp.addClassNames(FontSize.XXXLARGE, Margin.Horizontal.XSMALL, AlignSelf.CENTER);
			VerticalLayout data = setDataRound();
			this.add(lp,data);
		}

		private VerticalLayout setDataRound() {
			VerticalLayout data = new VerticalLayout();
			data.setWidth("75px");
			data.setHeight("75px");
			data.setJustifyContentMode(JustifyContentMode.CENTER);
			data.setAlignItems(Alignment.CENTER);
			Div weight = new Div(round.getWeight()+"");
			if (round.getWeight()==lastRound.getWeight()||lastRound.getWeight()==0)
				weight.addClassNames(GymStyles.EQUAL);
			else if(round.getWeight()<lastRound.getWeight())
				weight.addClassNames(GymStyles.WORSE);
			else if (round.getWeight()>lastRound.getWeight()) 
				weight.addClassNames(GymStyles.BETTER);
			Div repeats = new Div(round.getReps()+"");
			if(round.getReps()==lastRound.getReps()||lastRound.getReps()==0)
				repeats.addClassNames(GymStyles.EQUAL);
			else if(round.getReps()<lastRound.getReps())
				repeats.addClassNames(GymStyles.WORSE);
			else if(round.getReps()>lastRound.getReps())
				repeats.addClassNames(GymStyles.BETTER);
			data.add(weight,repeats);
			return data;
		}
		
	}
}
