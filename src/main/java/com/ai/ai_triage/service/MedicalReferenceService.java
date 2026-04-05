package com.ai.ai_triage.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class MedicalReferenceService {

    // Immutable value object — carried into the prompt builder
    public record ReferenceEntry(String topic, String title, String author, String url) {}

    private final List<ReferenceEntry> references = new ArrayList<>();

    public MedicalReferenceService() {
        try {
            InputStream is = getClass().getResourceAsStream("/references.json");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode array = mapper.readTree(is);
            array.forEach(node -> references.add(new ReferenceEntry(
                    node.get("topic").asText(),
                    node.get("title").asText(),
                    node.get("author").asText(),
                    node.get("url").asText()
            )));
        } catch (Exception e) {
            System.err.println("Could not load references: " + e.getMessage());
        }
    }

    /**
     * Returns the best-matching ReferenceEntry for the given symptoms.
     * Falls back to the first entry if nothing matches.
     */
    public ReferenceEntry findReferenceEntry(String symptoms) {
        if (symptoms == null || references.isEmpty()) {
            return new ReferenceEntry("general", "WHO Medical Guidelines",
                    "World Health Organization", "https://www.who.int");
        }
        String s = symptoms.toLowerCase();
        for (ReferenceEntry ref : references) {
            if (s.contains(ref.topic())) return ref;
        }
        // Default: first entry
        return references.get(0);
    }

    /**
     * Legacy string form — kept so nothing else breaks if it was used elsewhere.
     */
    public String findReference(String symptoms) {
        ReferenceEntry ref = findReferenceEntry(symptoms);
        return String.format("According to %s, \"%s\": %s", ref.author(), ref.title(), ref.url());
    }
}