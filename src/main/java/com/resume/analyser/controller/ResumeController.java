package com.resume.analyser.controller;

import com.resume.analyser.domain.Resume;
import com.resume.analyser.repository.ResumeRepository;
import com.resume.analyser.service.AIAnalysisService;
import com.resume.analyser.service.ResumeParserService;
import com.resume.analyser.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/resumes")
@CrossOrigin(origins = "http://localhost:3000") // Allow frontend
public class ResumeController {

    private final ResumeParserService parserService;
    private final AIAnalysisService analysisService;
    private final ResumeRepository repository;
    private final NotificationService notificationService;
    private final com.resume.analyser.service.InterviewService interviewService;

    public ResumeController(ResumeParserService parserService, AIAnalysisService analysisService,
            ResumeRepository repository, NotificationService notificationService,
            com.resume.analyser.service.InterviewService interviewService) {
        this.parserService = parserService;
        this.analysisService = analysisService;
        this.repository = repository;
        this.notificationService = notificationService;
        this.interviewService = interviewService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "jobDescription", required = false) String jobDescription) {
        try {
            String parsedContent = parserService.parse(file);
            AIAnalysisService.AnalysisResult analysis = analysisService.analyze(parsedContent, jobDescription);

            Resume resume = new Resume(file.getOriginalFilename(), parsedContent);
            resume.setAtsScore(analysis.atsScore());
            resume.setFeedback(analysis.feedback());
            resume.setEmail(analysis.email()); // Save extracted email

            // Set ATS Metrics
            resume.setSkillsCoverage(analysis.skillsCoverage());
            resume.setExperienceQuality(analysis.experienceQuality());
            resume.setFormattingIssues(analysis.formattingIssues());
            resume.setKeywords(analysis.keywords());

            if (jobDescription != null && !jobDescription.isBlank()) {
                resume.setJobDescription(jobDescription);
                resume.setMatchScore(analysis.matchScore());
                resume.setMissingSkills(analysis.missingSkills());
            }

            // Automate Interview Question Generation if Qualified
            Integer scoreToCheck = (resume.getMatchScore() != null) ? resume.getMatchScore() : resume.getAtsScore();
            if (scoreToCheck != null && scoreToCheck >= 70) {
                String questions = analysisService.generateInterviewQuestions(parsedContent, jobDescription);
                resume.setInterviewQuestions(questions);
            }

            Resume savedResume = repository.save(resume);
            notificationService.sendAnalysisCompleteNotification(savedResume);

            // Automated AI Interview Invitation (New Flow) - specific user request
            if (savedResume.getMatchScore() != null && savedResume.getMatchScore() > 70) {
                interviewService.createAndScheduleSession(savedResume.getId(), savedResume.getJobDescription());
            }

            return ResponseEntity.ok(savedResume);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error parsing file: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resume> getResume(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<java.util.List<Resume>> getAllResumes() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/qualified")
    public ResponseEntity<java.util.List<Resume>> getQualifiedResumes(@RequestParam(defaultValue = "70") int minScore) {
        // Filter logic could be moved to Repository for efficiency, but stream is fine
        // for now
        java.util.List<Resume> qualified = repository.findAll().stream()
                .filter(r -> r.getMatchScore() != null && r.getMatchScore() >= minScore)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(qualified);
    }

    @PostMapping("/rewrite")
    public ResponseEntity<String> rewriteSection(@RequestBody RewriteRequest request) {
        String rewrittenText = analysisService.rewriteSection(
                request.sectionContent, request.sectionName, request.targetRole, request.instruction);
        return ResponseEntity.ok(rewrittenText);
    }

    @PostMapping("/{id}/tailor")
    public ResponseEntity<String> tailorResume(@PathVariable Long id, @RequestParam String targetRole) {
        return repository.findById(id)
                .map(resume -> {
                    String tailoredContent = analysisService.tailorResume(resume.getParsedContent(), targetRole);
                    return ResponseEntity.ok(tailoredContent);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public record RewriteRequest(String sectionContent, String sectionName, String targetRole, String instruction) {
    }
}
