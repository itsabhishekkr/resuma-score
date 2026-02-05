package com.resume.analyser.controller;

import com.resume.analyser.domain.InterviewSession;
import com.resume.analyser.repository.InterviewSessionRepository;
import com.resume.analyser.service.AIAnalysisService;
import com.resume.analyser.service.InterviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/interview")
@CrossOrigin(origins = "http://localhost:3000") // Frontend URL
public class InterviewController {

    private final InterviewSessionRepository sessionRepository;
    private final InterviewService interviewService;
    private final AIAnalysisService aiAnalysisService;

    public InterviewController(InterviewSessionRepository sessionRepository,
            InterviewService interviewService,
            AIAnalysisService aiAnalysisService) {
        this.sessionRepository = sessionRepository;
        this.interviewService = interviewService;
        this.aiAnalysisService = aiAnalysisService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> getSession(@PathVariable String token) {
        Optional<InterviewSession> sessionOpt = sessionRepository.findByToken(token);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        InterviewSession session = sessionOpt.get();
        // Don't expose everything, maybe just status and relevant details
        return ResponseEntity.ok(Map.of(
                "status", session.getStatus(),
                "transcript", session.getTranscript()));
    }

    @PostMapping("/{token}/start")
    public ResponseEntity<?> startInterview(@PathVariable String token) {
        Optional<InterviewSession> sessionOpt = sessionRepository.findByToken(token);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        InterviewSession session = sessionOpt.get();

        if (session.getStatus() == InterviewSession.SessionStatus.COMPLETED) {
            return ResponseEntity.badRequest().body("Interview already completed.");
        }

        // Logic to get the first question
        // For simplicity, we generate the first question based on the job description
        String firstQuestion = "Hello, welcome to your AI interview. Can you please introduce yourself and briefly describe your experience relevant to this role?";

        session.setStatus(InterviewSession.SessionStatus.IN_PROGRESS);
        sessionRepository.save(session);

        return ResponseEntity.ok(Map.of("message", firstQuestion));
    }

    @PostMapping("/{token}/answer")
    public ResponseEntity<?> submitAnswer(@PathVariable String token, @RequestBody Map<String, String> payload) {
        Optional<InterviewSession> sessionOpt = sessionRepository.findByToken(token);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String answer = payload.get("answer");
        InterviewSession session = sessionOpt.get();

        // Append to transcript
        String currentTranscript = session.getTranscript() != null ? session.getTranscript() : "";
        session.setTranscript(currentTranscript + "\nCandidate: " + answer);
        sessionRepository.save(session);

        // Ask AI for next question
        String nextQuestion = aiAnalysisService.getChatResponse(session.getTranscript(), session.getJobDescription());

        // Append AI question to transcript so it persists in context
        session.setTranscript(session.getTranscript() + "\nAI: " + nextQuestion);
        sessionRepository.save(session);

        return ResponseEntity.ok(Map.of("message", nextQuestion));
    }
}
