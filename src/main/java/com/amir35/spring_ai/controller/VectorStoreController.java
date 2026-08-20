package com.amir35.spring_ai.controller;

import com.amir35.spring_ai.service.VectorStoreService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class VectorStoreController {

    private final VectorStoreService vectorStoreService;

    public VectorStoreController(VectorStoreService vectorStoreService) {

        this.vectorStoreService = vectorStoreService;
    }

    @PostMapping("/vector-store")
    public Map<String, String> saveToVectorStore(
            @RequestBody String text) {

        vectorStoreService.saveText(text);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Text saved successfully to vector store");

        return response;
    }
}