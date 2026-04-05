package com.ai.ai_triage.repository;

import com.ai.ai_triage.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {}