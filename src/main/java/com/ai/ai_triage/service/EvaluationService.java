package com.ai.ai_triage.service;

import com.ai.ai_triage.agent.MedicalAgent;
import com.ai.ai_triage.dto.TriageRequest;
import com.ai.ai_triage.entity.EvaluationResult;
import com.ai.ai_triage.repository.EvaluationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EvaluationService {

    private final MedicalAgent medicalAgent;
    private final EvaluationRepository evaluationRepository;

    public EvaluationService(MedicalAgent medicalAgent,
                             EvaluationRepository evaluationRepository) {
        this.medicalAgent = medicalAgent;
        this.evaluationRepository = evaluationRepository;
    }

    public static class EvalResult {
        public String symptoms;
        public String expectedRisk;
        public String actualRisk;
        public boolean correct;

        public EvalResult(String symptoms, String expectedRisk,
                          String actualRisk, boolean correct) {
            this.symptoms = symptoms;
            this.expectedRisk = expectedRisk;
            this.actualRisk = actualRisk;
            this.correct = correct;
        }
    }

    public static class EvalSummary {
        public List<EvalResult> results;
        public int total;
        public int correct;
        public double accuracy;

        public EvalSummary(List<EvalResult> results, int total,
                           int correct, double accuracy) {
            this.results = results;
            this.total = total;
            this.correct = correct;
            this.accuracy = accuracy;
        }
    }

    public EvalSummary runEvaluation() {
        List<String[]> testCases = List.of(
                new String[]{"fever and headache",                    "25", "none",         "medium"},
                new String[]{"chest pain and difficulty breathing",   "60", "heart disease","high"},
                new String[]{"mild runny nose",                       "30", "none",         "low"},
                new String[]{"severe abdominal pain and vomiting",    "45", "diabetes",     "high"},
                new String[]{"slight cough",                          "20", "none",         "low"}
        );

        List<EvalResult> results = new ArrayList<>();
        int correct = 0;

        for (String[] tc : testCases) {
            TriageRequest request = new TriageRequest();
            request.setSymptoms(tc[0]);
            request.setAge(Integer.parseInt(tc[1]));
            request.setMedicalHistory(tc[2]);
            request.setLanguage("en");

            String response = medicalAgent.analyze(request);
            String actualRisk = extractRiskLevel(response);
            boolean isCorrect = actualRisk.equalsIgnoreCase(tc[3]);
            if (isCorrect) correct++;

            evaluationRepository.save(
                    new EvaluationResult(tc[0], tc[3], actualRisk, isCorrect)
            );

            results.add(new EvalResult(tc[0], tc[3], actualRisk, isCorrect));
        }

        double accuracy = (double) correct / testCases.size() * 100;
        return new EvalSummary(results, testCases.size(), correct, accuracy);
    }

    /**
     * Extracts risk level from the LLM plain-text response.
     * The prompt instructs the LLM to write "Risk level: high/medium/low".
     */
    private String extractRiskLevel(String response) {
        if (response == null) return "unknown";
        try {
            // Primary: match "Risk level: high/medium/low" (case-insensitive)
            Pattern p = Pattern.compile("risk level\\s*[:–-]\\s*(high|medium|low)", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(response);
            if (m.find()) return m.group(1).toLowerCase();

            // Fallback keyword scan
            String lower = response.toLowerCase();
            if (lower.contains("high"))   return "high";
            if (lower.contains("medium")) return "medium";
            if (lower.contains("low"))    return "low";

        } catch (Exception e) {
            return "unknown";
        }
        return "unknown";
    }
}