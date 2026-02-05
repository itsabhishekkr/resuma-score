package com.resume.analyser.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ResumeParserService {

    public String parse(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.endsWith(".pdf")) {
            return parsePdf(file);
        }
        // Placeholder for DOCX or other formats
        return "Unsupported format";
    }

    private String parsePdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
