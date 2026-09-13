package com.github.dsquare68.gym.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dsquare68.gym.model.Training;
import com.github.dsquare68.gym.service.ExerciseNamesServiceImpl;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "gym_schema", name = "TRAININGS")
@Data
@Component
@AllArgsConstructor
public class TrainingRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQUENCE")
    @SequenceGenerator(sequenceName = "SIMPLE", allocationSize = 1, name = "SEQUENCE")
	@JsonProperty("ID")
	private Long ID;
	@JsonProperty("ID_TRAINING")
	private Long ID_TRAINING;
	@JsonProperty("ID_EXERCISE_NAME")
	private Long ID_EXERCISE_NAME;
	@JsonProperty("SERIE")
	private int SERIE;
	@JsonProperty("REPEAT")
	private int REPEAT;
	@JsonProperty("WEIGHT")
	private double WEIGHT;
	@JsonProperty("SCHEMA")
	private int SCHEMA;
	@JsonProperty("IS_SCHEMA")
	private int IS_SCHEMA;
	@JsonProperty("DATE_TRAINING")
	private LocalDate DATE_TRAINING;
	@JsonProperty("NAME_SCHEMA")
	private String NAME_SCHEMA;
	@JsonProperty("TIME_TRAINING")
	private String TIME_TRAINING;
	
	public TrainingRecord(Long id_training, Long id_exercise_name, int serie, int repeat, double weight,
			int schema, int is_schema, LocalDate date_training, String name_schema) {
		this.ID_TRAINING = id_training;
		this.ID_EXERCISE_NAME = id_exercise_name;
		this.SERIE = serie;
		this.REPEAT = repeat;
		this.WEIGHT = weight;
		this.SCHEMA = schema;
		this.IS_SCHEMA = is_schema;
		this.DATE_TRAINING = date_training;
		this.NAME_SCHEMA = name_schema;
	}

	public TrainingRecord() {
		// Default constructor
	}
	public static Training toTraining(ArrayList<TrainingRecord> records, ExerciseNamesServiceImpl service){
        Training tr = new Training(records.get(0).getNAME_SCHEMA());
        String exerciseName = "";
        int exerciseLP = -1;
        int serieLP = 0;
        tr.setID(records.get(0).getID_TRAINING());
        tr.setDate(records.get(0).getDATE_TRAINING());
        tr.setSchema(records.get(0).getSCHEMA());
        if(records.get(0).getIS_SCHEMA()==1)
            tr.setTemplete(true);
        else
            tr.setTemplete(false);
        for(TrainingRecord r : records){
            String name = service.repo.findById(r.getID_EXERCISE_NAME()).get().getName();
            if(!exerciseName.equals(name)) {
                tr.getExercises().add(name);
                exerciseName=name;
                exerciseLP++;
            }
            if(r.getSERIE()==1) {
                tr.getRounds().put(tr.getExercises().get(exerciseLP), new ArrayList<>());
            }
            tr.getRounds().get(tr.getExercises().get(exerciseLP)).add(tr.new Round(r.getSERIE(),r.getREPEAT(),r.getWEIGHT()));

        }
        return tr;
    }
	public static ArrayList<Training> toTraininings(ArrayList<TrainingRecord> records, ExerciseNamesServiceImpl service){
		if(records==null||records.size()==0)
			return null;
		ArrayList<Training> result = new ArrayList<Training>();
		ArrayList<TrainingRecord> training = new ArrayList<TrainingRecord>();
		Long oldId = records.get(0).getID_TRAINING();
		for(TrainingRecord tr : records) {
			Long id = tr.getID_TRAINING();
			if(id==oldId)
				training.add(tr);
			else {
				result.add(TrainingRecord.toTraining(training, service));
				oldId=id;
				training = new ArrayList<TrainingRecord>();
				training.add(tr);
			}
			
		}
		result.add(TrainingRecord.toTraining(training, service));
 		return result;
	}
}
