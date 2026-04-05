package com.ai.ai_triage.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_record")
public class ChatRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String symptoms;
    private int age;
    private String medicalHistory;
    private String language;

    @Column(length = 3000)
    private String llmResponse;
    private String ruleBasedRisk;
    private LocalDateTime createdAt;

    public ChatRecord() {}
    public ChatRecord(String symptoms, int age, String medicalHistory,
                      String language, String llmResponse, String ruleBasedRisk) {
        this.symptoms = symptoms;
        this.age = age;
        this.medicalHistory = medicalHistory;
        this.language = language;
        this.llmResponse = llmResponse;
        this.ruleBasedRisk = ruleBasedRisk;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getSymptoms() { return symptoms; }
    public int getAge() { return age; }
    public String getMedicalHistory() { return medicalHistory; }
    public String getLanguage() { return language; }
    public String getLlmResponse() { return llmResponse; }
    public String getRuleBasedRisk() { return ruleBasedRisk; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}