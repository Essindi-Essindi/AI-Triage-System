package com.ai.ai_triage.service;

import com.ai.ai_triage.dto.FeedbackRequest;
import com.ai.ai_triage.entity.Feedback;
import com.ai.ai_triage.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public void save(FeedbackRequest request) {
        feedbackRepository.save(
                new Feedback(request.getRating(), request.getComment())
        );
    }

    public List<Feedback> getAll() {
        return feedbackRepository.findAll();
    }

    public Map<String, Object> getSummary() {
        List<Feedback> all = feedbackRepository.findAll();

        long useful = all.stream()
                .filter(f -> "useful".equalsIgnoreCase(f.getRating()))
                .count();

        long notUseful = all.stream()
                .filter(f -> "not useful".equalsIgnoreCase(f.getRating()))
                .count();

        double usefulRate = all.isEmpty() ? 0 :
                (double) useful / all.size() * 100;

        return Map.of(
                "total",      all.size(),
                "useful",     useful,
                "notUseful",  notUseful,
                "usefulRate", usefulRate,
                "comments",   all.stream()
                        .map(Feedback::getComment)
                        .filter(c -> c != null && !c.isBlank())
                        .toList()
        );
    }
}