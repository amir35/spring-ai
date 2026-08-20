package com.amir35.spring_ai.controller;

import com.amir35.spring_ai.service.VectorStoreService;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class SearchController {

    private final VectorStoreService vectorStoreService;

    public SearchController(VectorStoreService vectorStoreService) {

        this.vectorStoreService = vectorStoreService;
    }

    @PostMapping("/search")
    public List<Document> search(
            @RequestBody String query) {

        return vectorStoreService.search(query);
    }
}