package com.ai.ai_triage.dto;

public class TriageRequest {
    private String symptoms;
    private int age;
    private String medicalHistory;
    private String language; // "en" or "fr" — RC5

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}