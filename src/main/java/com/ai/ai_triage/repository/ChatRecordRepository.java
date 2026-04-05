package com.ai.ai_triage.repository;

import com.ai.ai_triage.entity.ChatRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRecordRepository extends JpaRepository<ChatRecord, Long> {}