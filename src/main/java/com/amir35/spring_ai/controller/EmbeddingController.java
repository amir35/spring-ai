package com.amir35.spring_ai.controller;

import com.amir35.spring_ai.service.EmbeddingService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    public EmbeddingController(EmbeddingService embeddingService) {

        this.embeddingService = embeddingService;
    }

    @PostMapping("/embedding")
    public Map<String, Object> generateEmbedding(@RequestBody String text) {

        float[] embedding = embeddingService.generateEmbedding(text);

        Map<String, Object> response = new HashMap<>();
        response.put("text", text);
        response.put("dimension", embedding.length);
        response.put("embedding", embedding);

        return response;
    }
}