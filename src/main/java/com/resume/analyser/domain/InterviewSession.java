package com.resume.analyser.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    private Long resumeId;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @Column(columnDefinition = "LONGTEXT")
    private String transcript;

    private Integer overallScore;

    @Column(columnDefinition = "TEXT")
    private String aiFeedback;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public InterviewSession() {
    }

    public InterviewSession(String token, Long resumeId, String jobDescription) {
        this.token = token;
        this.resumeId = resumeId;
        this.jobDescription = jobDescription;
        this.status = SessionStatus.SCHEDULED;
        this.createdAt = LocalDateTime.now();
        this.transcript = "";
    }

    public enum SessionStatus {
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public String getAiFeedback() {
        return aiFeedback;
    }

    public void setAiFeedback(String aiFeedback) {
        this.aiFeedback = aiFeedback;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
