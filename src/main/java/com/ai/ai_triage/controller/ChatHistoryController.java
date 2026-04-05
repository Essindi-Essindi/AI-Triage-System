package com.ai.ai_triage.controller;

import com.ai.ai_triage.entity.ChatRecord;
import com.ai.ai_triage.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatHistoryController {

    private final ChatService chatService;

    public ChatHistoryController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/chat/history")
    public ResponseEntity<List<ChatRecord>> getHistory() {
        return ResponseEntity.ok(chatService.getAll());
    }
}