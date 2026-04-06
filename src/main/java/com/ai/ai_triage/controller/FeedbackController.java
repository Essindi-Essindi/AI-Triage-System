package com.ai.ai_triage.controller;

import com.ai.ai_triage.dto.FeedbackRequest;
import com.ai.ai_triage.entity.Feedback;
import com.ai.ai_triage.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/feedback")
    public ResponseEntity<String> submitFeedback(@RequestBody FeedbackRequest feedback) {
        feedbackService.save(feedback);
        return ResponseEntity.ok("Feedback received. Thank you.");
    }

    @GetMapping("/feedback/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(feedbackService.getSummary());
    }

    @GetMapping("/feedback/all")
    public ResponseEntity<List<Feedback>> getAllFeedback() {
        return ResponseEntity.ok(feedbackService.getAll());
    }

}