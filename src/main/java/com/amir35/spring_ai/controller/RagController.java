package com.amir35.spring_ai.controller;

import com.amir35.spring_ai.service.RagService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/ask")
    public Map<String, Object> ask(
            @RequestBody String question) {

        String answer = ragService.askQuestion(question);

        return Map.of(
                "question", question,
                "answer", answer
        );
    }
}