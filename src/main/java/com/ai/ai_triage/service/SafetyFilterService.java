package com.ai.ai_triage.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SafetyFilterService {

    // Keywords that suggest unsafe or overconfident output
    private static final List<String> DANGEROUS_KEYWORDS = List.of(
            "you have cancer",
            "you are dying",
            "take this medication",
            "you have diabetes",
            "you have covid",
            "diagnosed with"
    );

    public String filter(String response) {
        if (response == null) return buildSafetyWarning();

        String lower = response.toLowerCase();
        for (String keyword : DANGEROUS_KEYWORDS) {
            if (lower.contains(keyword)) {
                return buildSafetyWarning();
            }
        }

        return response;
    }

    /**
     * Returns a plain-text safety warning that the frontend can display directly.
     * Previously this returned a JSON blob, which the chat component rendered as raw text.
     */
    private String buildSafetyWarning() {
        return "I'm sorry, but the response I generated may have contained unsafe or overconfident medical language and has been blocked for your safety.\n\n"
                + "Risk level: unknown\n\n"
                + "I strongly recommend that you consult a qualified physician or visit your nearest medical facility immediately. "
                + "Please do not rely on this system for medical decisions.";
    }
}