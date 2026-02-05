package com.resume.analyser.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String email;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String parsedContent;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String feedback;

    private Integer atsScore;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    private Integer matchScore;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String missingSkills;

    // ATS Specific Fields
    private Integer skillsCoverage;

    private String experienceQuality; // High, Medium, Low

    @Lob
    @Column(columnDefinition = "TEXT")
    private String formattingIssues;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String keywords;

    // Interview Results
    private Integer interviewScore;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String interviewFeedback;

    public Resume() {
    }

    public Resume(String fileName, String parsedContent) {
        this.fileName = fileName;
        this.parsedContent = parsedContent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getParsedContent() {
        return parsedContent;
    }

    public void setParsedContent(String parsedContent) {
        this.parsedContent = parsedContent;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Integer getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(Integer atsScore) {
        this.atsScore = atsScore;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public Integer getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(Integer matchScore) {
        this.matchScore = matchScore;
    }

    public String getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(String missingSkills) {
        this.missingSkills = missingSkills;
    }

    public Integer getSkillsCoverage() {
        return skillsCoverage;
    }

    public void setSkillsCoverage(Integer skillsCoverage) {
        this.skillsCoverage = skillsCoverage;
    }

    public String getExperienceQuality() {
        return experienceQuality;
    }

    public void setExperienceQuality(String experienceQuality) {
        this.experienceQuality = experienceQuality;
    }

    public String getFormattingIssues() {
        return formattingIssues;
    }

    public void setFormattingIssues(String formattingIssues) {
        this.formattingIssues = formattingIssues;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Integer getInterviewScore() {
        return interviewScore;
    }

    public void setInterviewScore(Integer interviewScore) {
        this.interviewScore = interviewScore;
    }

    public String getInterviewFeedback() {
        return interviewFeedback;
    }

    public void setInterviewFeedback(String interviewFeedback) {
        this.interviewFeedback = interviewFeedback;
    }

    @Lob
    @Column(columnDefinition = "TEXT")
    private String interviewQuestions;

    public String getInterviewQuestions() {
        return interviewQuestions;
    }

    public void setInterviewQuestions(String interviewQuestions) {
        this.interviewQuestions = interviewQuestions;
    }
}
