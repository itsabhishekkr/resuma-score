package com.resume.analyser.repository;

import com.resume.analyser.domain.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    Optional<InterviewSession> findByToken(String token);
}
