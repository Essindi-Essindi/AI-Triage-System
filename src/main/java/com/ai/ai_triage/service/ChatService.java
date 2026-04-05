package com.ai.ai_triage.service;

import com.ai.ai_triage.entity.ChatRecord;
import com.ai.ai_triage.repository.ChatRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final ChatRecordRepository chatRecordRepository;

    public ChatService(ChatRecordRepository chatRecordRepository) {
        this.chatRecordRepository = chatRecordRepository;
    }

    public void save(String symptoms, int age, String medicalHistory,
                     String language, String llmResponse, String ruleRisk) {
        chatRecordRepository.save(new ChatRecord(
                symptoms, age, medicalHistory, language, llmResponse, ruleRisk
        ));
    }

    public List<ChatRecord> getAll() {
        return chatRecordRepository.findAll();
    }
}