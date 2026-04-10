package com.ai.ai_triage.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class MedicalReferenceService {

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
     * Returns ALL matching ReferenceEntries for the given symptoms.
     * Falls back to the first entry if nothing matches.
     */
    public List<ReferenceEntry> findAllMatchingReferences(String symptoms) {
        if (symptoms == null || references.isEmpty()) {
            return List.of(new ReferenceEntry("general", "WHO Medical Guidelines",
                    "World Health Organization", "https://www.who.int"));
        }

        String s = symptoms.toLowerCase();
        List<ReferenceEntry> matched = new ArrayList<>();

        for (ReferenceEntry ref : references) {
            if (s.contains(ref.topic())) {
                matched.add(ref);
            }
        }

        // Fallback to first entry if no match
        if (matched.isEmpty()) {
            matched.add(references.get(0));
        }

        return matched;
    }

    /**
     * Legacy single-entry lookup — kept for backward compatibility.
     */
    public ReferenceEntry findReferenceEntry(String symptoms) {
        return findAllMatchingReferences(symptoms).get(0);
    }

    public String findReference(String symptoms) {
        ReferenceEntry ref = findReferenceEntry(symptoms);
        return String.format("According to %s, \"%s\": %s", ref.author(), ref.title(), ref.url());
    }
}