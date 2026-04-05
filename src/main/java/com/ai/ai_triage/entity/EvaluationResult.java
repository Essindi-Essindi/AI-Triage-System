package com.ai.ai_triage.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "evaluation_result")
public class EvaluationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String symptoms;
    private String expectedRisk;
    private String actualRisk;
    private boolean correct;

    public EvaluationResult() {}
    public EvaluationResult(String symptoms, String expectedRisk, String actualRisk, boolean correct) {
        this.symptoms = symptoms;
        this.expectedRisk = expectedRisk;
        this.actualRisk = actualRisk;
        this.correct = correct;
    }

    public Long getId() { return id; }
    public String getSymptoms() { return symptoms; }
    public String getExpectedRisk() { return expectedRisk; }
    public String getActualRisk() { return actualRisk; }
    public boolean isCorrect() { return correct; }
}