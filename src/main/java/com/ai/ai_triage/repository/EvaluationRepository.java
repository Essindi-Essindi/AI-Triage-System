package com.ai.ai_triage.repository;

import com.ai.ai_triage.entity.EvaluationResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<EvaluationResult, Long> {}