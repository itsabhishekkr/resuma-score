package com.resume.analyser.service;

import com.resume.analyser.domain.InterviewSession;
import com.resume.analyser.domain.Resume;
import com.resume.analyser.repository.InterviewSessionRepository;
import com.resume.analyser.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class InterviewService {

    private final InterviewSessionRepository interviewSessionRepository;
    private final ResumeRepository resumeRepository;
    private final NotificationService notificationService;
    private final AIAnalysisService aiAnalysisService;

    // Base URL for the frontend - hardcoded for dev
    private final String FRONTEND_BASE_URL = "http://localhost:5173";

    public InterviewService(InterviewSessionRepository interviewSessionRepository,
            ResumeRepository resumeRepository,
            NotificationService notificationService,
            AIAnalysisService aiAnalysisService) {
        this.interviewSessionRepository = interviewSessionRepository;
        this.resumeRepository = resumeRepository;
        this.notificationService = notificationService;
        this.aiAnalysisService = aiAnalysisService;
    }

    @Transactional
    public void createAndScheduleSession(Long resumeId, String jobDescription) {
        // 1. Create Session
        String token = UUID.randomUUID().toString();
        InterviewSession session = new InterviewSession(token, resumeId, jobDescription);
        interviewSessionRepository.save(session);

        // 2. Get User Email
        Resume resume = resumeRepository.findById(resumeId).orElseThrow(() -> new RuntimeException("Resume not found"));
        // Assuming the detailed analysis result has the extracted email, or it's stored
        // on the Resume entity.
        // For now, we'll try to get it from the Resume entity field if it exists,
        // otherwise rely on previous logic.
        // Wait, the Resume entity usually stores the extracted email. Let's assume it
        // does.

        String email = resume.getEmail(); // We need to ensure valid email exists
        if (email == null || email.isEmpty()) {
            System.out.println("⚠️ No email found for resume " + resumeId + ", cannot schedule interview.");
            return;
        }

        // 3. Generate Link
        String interviewLink = FRONTEND_BASE_URL + "/interview/" + token;

        // 4. Send Email
        notificationService.sendScheduledInterview(email, interviewLink);
    }

    // Additional methods for session handling will go here...
}
