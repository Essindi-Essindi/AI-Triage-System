package com.ai.ai_triage.service;

import org.springframework.stereotype.Service;

@Service
public class RuleBasedService {

    // Simple if/else risk classification — used to compare against LLM (RC3)
    public String classify(String symptoms) {
        if (symptoms == null) return "unknown";

        String s = symptoms.toLowerCase();

        if (s.contains("chest pain") || s.contains("difficulty breathing")
                || s.contains("unconscious") || s.contains("stroke")
                || s.contains("severe bleeding")) {
            return "high";
        }

        if (s.contains("fever") || s.contains("headache")
                || s.contains("vomiting") || s.contains("abdominal pain")
                || s.contains("dizziness")) {
            return "medium";
        }

        if (s.contains("cough") || s.contains("runny nose")
                || s.contains("sore throat") || s.contains("fatigue")) {
            return "low";
        }

        return "unknown";
    }
}