package com.ai.ai_triage.controller;

import com.ai.ai_triage.agent.MedicalAgent;
import com.ai.ai_triage.dto.TriageRequest;
import com.ai.ai_triage.service.ChatService;
import com.ai.ai_triage.service.RuleBasedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TriageController {

    private final MedicalAgent medicalAgent;
    private final RuleBasedService ruleBasedService;
    private final ChatService chatService;

    public TriageController(MedicalAgent medicalAgent,
                            RuleBasedService ruleBasedService,
                            ChatService chatService) {
        this.medicalAgent = medicalAgent;
        this.ruleBasedService = ruleBasedService;
        this.chatService = chatService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyze(@RequestBody TriageRequest request) {
        String llmResult = medicalAgent.analyze(request);
        String ruleResult = ruleBasedService.classify(request.getSymptoms());

        // Save to chat history DB
        chatService.save(
                request.getSymptoms(),
                request.getAge(),
                request.getMedicalHistory(),
                request.getLanguage(),
                llmResult,
                ruleResult
        );

        return ResponseEntity.ok(Map.of(
                "llm_result", llmResult,
                "rule_based_risk", ruleResult
        ));
    }
}