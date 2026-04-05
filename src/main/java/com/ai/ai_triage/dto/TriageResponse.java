package com.ai.ai_triage.dto;

public class TriageResponse {
    private String symptoms;
    private String riskLevel;
    private String recommendations;

    public TriageResponse(String symptoms, String riskLevel, String recommendations) {
        this.symptoms = symptoms;
        this.riskLevel = riskLevel;
        this.recommendations = recommendations;
    }

    public String getSymptoms() { return symptoms; }
    public String getRiskLevel() { return riskLevel; }
    public String getRecommendations() { return recommendations; }
}