package com.github.dsquare68.gym.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import lombok.Data;

@Data
public class Training {
	 private Long ID;
	    private String name;
	    private LocalDate date;
	    private int schema;
	    private LocalTime timeOfTrainig;
	    private ArrayList<String> exercises;
	    private HashMap<String,ArrayList<Round>> rounds;
	    private boolean templete;

	    public Training(Long _ID, ArrayList<String> exercises, HashMap<String, ArrayList<Round>> rounds) {
	        this.ID = _ID;
	        this.exercises = exercises;
	        this.rounds = rounds;
	    }
	    public Training(String name){
	        this.name = name;
	        templete = false;
	        exercises = new ArrayList<>();
	        rounds = new HashMap<>();
	    }
	    public Training(){
	        this.name = "Empty";
	        templete = false;
	        exercises = new ArrayList<>();
	        exercises.add("a");
	        exercises.add("b");
	        exercises.add("c");
	        exercises.add("d");
	        exercises.add("e");
	        exercises.add("f");
	        exercises.add("g");
	        exercises.add("h");
	        exercises.add("j");
	        exercises.add("k");
	        exercises.add("l");
	        rounds = new HashMap<>();
	        for(String s : exercises) {
	        	ArrayList<Training.Round> rs = new ArrayList<Training.Round>();
	        	Training.Round r1= new Training.Round(1, 0, 0);
	        	Training.Round r2= new Training.Round(2, 0, 0);
	        	Training.Round r3= new Training.Round(3, 0, 0);
	        	Training.Round r4= new Training.Round(4, 0, 0);
	        	rs.add(r1);
	        	rs.add(r2);
	        	rs.add(r3);
	        	rs.add(r4);
	        	rounds.put(s, rs);
	        }
	    }

	    
	    public class Round {
	        int roundNumber,reps;
	        double weight;

	        public Round(int roundNumber, int reps, double weight) {
	            this.roundNumber = roundNumber;
	            this.reps = reps;
	            this.weight = weight;
	        }

	        public int getRoundNumber() {
	            return roundNumber;
	        }

	        public void setRoundNumber(int roundNumber) {
	            this.roundNumber = roundNumber;
	        }

	        public int getReps() {
	            return reps;
	        }

	        public void setReps(int reps) {
	            this.reps = reps;
	        }

	        public double getWeight() {
	            return weight;
	        }

	        public void setWeight(double weight) {
	            this.weight = weight;
	        }
	    }
}
