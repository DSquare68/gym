package com.github.dsquare68.gym.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(
        name = "exercise_names",
        uniqueConstraints = @UniqueConstraint(name = "uq_exercise_names_name", columnNames = "name"),
        indexes = @Index(name = "idx_exercise_names_category", columnList = "category"))
public class ExerciseName {

    /** Whether a row was seeded from file or created by the user. */
    public enum Source {
        SEED,
        USER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 64)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Source source = Source.SEED;

    /** JPA. */
    protected ExerciseName() {
    }

    public ExerciseName(String name, String category, Source source) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Exercise name must not be blank");
        }
        this.name = name.trim();
        this.category = category;
        this.source = source == null ? Source.SEED : source;
    }

    /** A not-yet-persisted row read from the seed file. */
    public static ExerciseName seeded(String name, String category) {
        return new ExerciseName(name, category, Source.SEED);
    }

    /** A not-yet-persisted row the user added through the app. */
    public static ExerciseName added(String name, String category) {
        return new ExerciseName(name, category, Source.USER);
    }

    @Override
    public String toString() {
        return "ExerciseName[" + id + " " + name + "]";
    }
}
