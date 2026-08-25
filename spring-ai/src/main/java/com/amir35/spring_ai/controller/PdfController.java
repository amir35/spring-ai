package com.amir35.spring_ai.controller;

import com.amir35.spring_ai.service.PdfIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class PdfController {

    private final PdfIngestionService pdfIngestionService;

    public PdfController(PdfIngestionService pdfIngestionService) {
        this.pdfIngestionService = pdfIngestionService;
    }

    @PostMapping("/ingest-pdf")
    public ResponseEntity<Map<String, Object>> ingestPdf(
            @RequestParam("file") MultipartFile file) throws IOException {

        int chunks = pdfIngestionService.ingestPdf(
                        file.getResource()
                );

        return ResponseEntity.ok(
                Map.of(
                        "message", "PDF ingested successfully",
                        "fileName", file.getOriginalFilename(),
                        "chunks", chunks
                )
        );
    }
}