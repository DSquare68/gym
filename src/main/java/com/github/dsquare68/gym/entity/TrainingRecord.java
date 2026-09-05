package com.github.dsquare68.gym.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(
        name = "trainings",
        indexes = {
                @Index(name = "idx_trainings_user_performed", columnList = "user_id, performed_on"),
                @Index(name = "idx_trainings_scheme", columnList = "scheme_id")
        })
public class TrainingRecord {

    /** Which client wrote the row. */
    public enum Source {
        /** Entered through the plugin's own Vaadin UI. */
        WEB,
        /** Pushed in by an external client over the API. */
        API
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;
    

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean template;
    
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "exercise_name_id", nullable = false)
    private ExerciseName exerciseName;
    
    /**
     * The scheme this session was built from. {@code null} for a scheme itself,
     * and for a one-off session logged without a plan.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheme_id")
    private TrainingRecord scheme;

    @Column(name = "performed_on")
    private LocalDate performedOn;
    
    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Column
    private Integer reps;

    @Column(precision = 6, scale = 2)
    private BigDecimal weight;

    /** JPA. */
    protected TrainingRecord() {
    }

	public int getIS_SCHEMA() {
		// TODO Auto-generated method stub
		return 0;
	}
}
