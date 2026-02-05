package com.resume.analyser.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AIAnalysisService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public AnalysisResult analyze(String resumeText, String jobDescription) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                + geminiApiKey;

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Analyze the following resume text");
        if (jobDescription != null && !jobDescription.isBlank()) {
            promptBuilder.append(" against the provided Job Description.");
        } else {
            promptBuilder.append(".");
        }
        promptBuilder.append(" Perform a deep ATS analysis.");
        promptBuilder.append(" Format the response strictly as JSON with the following keys: ");
        promptBuilder.append("'atsScore' (integer 0-100), ");
        promptBuilder.append("'feedback' (string), ");
        promptBuilder.append("'email' (string, extract candidate email or null), ");

        // ATS Specific Fields
        promptBuilder.append("'skillsCoverage' (integer 0-100, estimated based on inferred role), ");
        promptBuilder.append("'experienceQuality' (string, 'High', 'Medium', or 'Low'), ");
        promptBuilder.append("'formattingIssues' (string, comma separated potential issues), ");
        promptBuilder.append("'keywords' (string, comma separated top 10 keywords found), ");

        if (jobDescription != null && !jobDescription.isBlank()) {
            promptBuilder.append("'matchScore' (integer 0-100), ");
            promptBuilder.append("'missingSkills' (string, comma separated), ");
        }
        promptBuilder.append("Ensure the JSON is valid and does not contain markdown formatting like ```json.");

        promptBuilder.append("\n\nResume Text:\n")
                .append(resumeText.substring(0, Math.min(resumeText.length(), 10000)));
        if (jobDescription != null && !jobDescription.isBlank()) {
            promptBuilder.append("\n\nJob Description:\n")
                    .append(jobDescription.substring(0, Math.min(jobDescription.length(), 5000)));
        }

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", promptBuilder.toString())))));

        try {
            Map<String, Object> response = sendRequestWithRetry(url, requestBody);
            return parseGeminiResponse(response, jobDescription != null && !jobDescription.isBlank());
        } catch (Exception e) {
            e.printStackTrace();
            return new AnalysisResult(0, "Error connecting to AI service: " + e.getMessage(), null, null, null, 0,
                    "Low",
                    "None", "");
        }
    }

    @SuppressWarnings("unchecked")
    private AnalysisResult parseGeminiResponse(Map<String, Object> response, boolean includeMatchData) {
        try {
            // Traverse the deeply nested JSON structure of Gemini API
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String text = (String) parts.get(0).get("text");

            // Simple parsing assuming clear JSON structure from prompt
            String cleanJson = text.replaceAll("```json", "").replaceAll("```", "").trim();

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> result = mapper.readValue(cleanJson, Map.class);

            Integer atsScore = result.get("atsScore") != null ? (Integer) result.get("atsScore") : 0;
            String feedback = (String) result.get("feedback");
            String email = (String) result.get("email"); // Extract Email
            Integer matchScore = null;
            String missingSkills = null;

            Integer skillsCoverage = result.get("skillsCoverage") != null ? (Integer) result.get("skillsCoverage") : 0;
            String experienceQuality = (String) result.get("experienceQuality");
            String formattingIssues = (String) result.get("formattingIssues");
            String keywords = (String) result.get("keywords");

            if (includeMatchData) {
                matchScore = result.get("matchScore") != null ? (Integer) result.get("matchScore") : 0;
                missingSkills = (String) result.get("missingSkills");
            }

            return new AnalysisResult(atsScore, feedback, email, matchScore, missingSkills, skillsCoverage,
                    experienceQuality,
                    formattingIssues, keywords);
        } catch (Exception e) {
            return new AnalysisResult(0, "Raw AI Response (Parsing Failed): " + e.getMessage(), null, null, null, 0,
                    "Low",
                    "Parsing Failed", "");
        }
    }

    public record AnalysisResult(
            int atsScore,
            String feedback,
            String email, // Added email
            Integer matchScore,
            String missingSkills,
            Integer skillsCoverage,
            String experienceQuality,
            String formattingIssues,
            String keywords) {
    }

    public String getChatResponse(String currentTranscript, String jobDescription) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                + geminiApiKey;

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI interviewer conducting an interview for a role described as follows:\n");
        prompt.append(jobDescription != null ? jobDescription.substring(0, Math.min(jobDescription.length(), 2000))
                : "General Role");
        prompt.append("\n\nHere is the interview transcript so far:\n");
        prompt.append(currentTranscript);
        prompt.append(
                "\n\nBased on the candidate's last response, ask the next relevant interview question. Keep it professional, encouraging, and concise. If the interview seems complete (e.g. 5-6 exchanges), kindly conclude the interview.");

        return callGemini(url, prompt.toString());
    }

    public String rewriteSection(String sectionContent, String sectionName, String targetRole, String instruction) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                + geminiApiKey;

        StringBuilder prompt = new StringBuilder();
        prompt.append("Rewrite the following ").append(sectionName).append(" section of a resume.");
        if (targetRole != null && !targetRole.isBlank()) {
            prompt.append(" Optimize it for a ").append(targetRole).append(" role.");
        }
        if (instruction != null && !instruction.isBlank()) {
            prompt.append(" Instruction: ").append(instruction);
        }
        prompt.append(" Use the STAR method for bullet points. Ensure high impact and use metrics where possible.");
        prompt.append("\n\nOriginal Text:\n").append(sectionContent);

        return callGemini(url, prompt.toString());
    }

    public String tailorResume(String resumeText, String targetRole) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                + geminiApiKey;

        String prompt = "Tailor the following resume for a " + targetRole + " role. " +
                "Optimize the summary, highlight relevant projects, and adjust keywords. " +
                "Return the full rewritten resume text.\n\nResume Text:\n"
                + resumeText.substring(0, Math.min(resumeText.length(), 10000));

        return callGemini(url, prompt);
    }

    public String generateInterviewQuestions(String resumeText, String jobDescription) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                + geminiApiKey;

        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate 5-7 technical and behavioral interview questions based on the following resume");
        if (jobDescription != null && !jobDescription.isBlank()) {
            prompt.append(" and the provided Job Description.");
        }
        prompt.append(" The candidate is being considered for a role matching their skills.");
        prompt.append("\n\nResume Text:\n").append(resumeText.substring(0, Math.min(resumeText.length(), 10000)));
        if (jobDescription != null && !jobDescription.isBlank()) {
            prompt.append("\n\nJob Description:\n")
                    .append(jobDescription.substring(0, Math.min(jobDescription.length(), 5000)));
        }

        return callGemini(url, prompt.toString());
    }

    private String callGemini(String url, String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)))));

        try {
            Map<String, Object> response = sendRequestWithRetry(url, requestBody);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating content: " + e.getMessage();
        }
    }

    private Map<String, Object> sendRequestWithRetry(String url, Map<String, Object> requestBody) {
        int maxRetries = 5;
        int waitTime = 10000; // 10 seconds initial wait

        for (int i = 0; i < maxRetries; i++) {
            try {
                return restTemplate.postForObject(url, requestBody, Map.class);
            } catch (HttpClientErrorException.TooManyRequests e) {
                System.out.println("⚠️ Quota Exceeded. Retrying in " + waitTime + "ms... (Attempt " + (i + 1) + ")");
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
                waitTime += 5000; // Increase wait time by 5s each time
            } catch (Exception e) {
                throw e; // Rethrow other exceptions
            }
        }
        throw new RuntimeException("API Quota Exceeded after " + maxRetries + " retries.");
    }
}
