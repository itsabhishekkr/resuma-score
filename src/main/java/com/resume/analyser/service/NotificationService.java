package com.resume.analyser.service;

import com.resume.analyser.domain.Resume;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class NotificationService {

    private final org.springframework.mail.javamail.JavaMailSender mailSender;

    public NotificationService(org.springframework.mail.javamail.JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public CompletableFuture<Void> sendAnalysisCompleteNotification(Resume resume) {
        // Placeholder for future implementation
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public void sendScheduledInterview(String email, String interviewLink) {
        String meetingTime = java.time.LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).toString();
        String subject = "Congratulations! You've been Shortlisted for an AI Interview";
        String body = "Congratulations! Your profile has been shortlisted.\n" +
                "We have scheduled a 1:1 interaction with our recruiting team via our AI Platform.\n" +
                "--------------------------------------------------\n" +
                "🗓️ Date & Time: " + meetingTime + "\n" +
                "📍 Link: " + interviewLink + "\n" +
                "--------------------------------------------------\n" +
                "Please click the link above to start your interview at the scheduled time.\n" +
                "This is an automated AI interview process. Good luck!";

        System.out.println("Sending interview invite to: " + email);

        try {
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("23f1003112@ds.study.iitm.ac.in");

            mailSender.send(message);
            System.out.println("✅ Interview invitation sent successfully to " + email);
        } catch (Exception e) {
            System.err.println("❌ Failed to send interview invitation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Weekly Cron Job: Every Monday at 9 AM
    // @Scheduled(cron = "0 0 9 * * MON")
    public void sendWeeklyTips() {
        System.out.println(">>> CRON JOB: Sending weekly resume improvement tips to all users...");
    }
}
