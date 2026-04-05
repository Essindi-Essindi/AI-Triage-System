package com.ai.ai_triage.controller;

import com.ai.ai_triage.service.EvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping("/evaluate")
    public ResponseEntity<EvaluationService.EvalSummary> evaluate() {
        EvaluationService.EvalSummary summary = evaluationService.runEvaluation();
        return ResponseEntity.ok(summary);
    }
}