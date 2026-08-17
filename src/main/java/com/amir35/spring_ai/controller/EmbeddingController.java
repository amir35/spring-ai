package com.amir35.spring_ai.controller;

import com.amir35.spring_ai.service.EmbeddingService;
import com.amir35.spring_ai.service.VectorStoreService;
import org.springframework.web.bind.annotation.*;

import org.springframework.ai.document.Document;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class EmbeddingController {

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;

    public EmbeddingController(
            EmbeddingService embeddingService,
            VectorStoreService vectorStoreService) {

        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
    }

    @PostMapping("/embedding")
    public Map<String, Object> generateEmbedding(
            @RequestBody String text) {

        float[] embedding =
                embeddingService.generateEmbedding(text);

        Map<String, Object> response = new HashMap<>();
        response.put("text", text);
        response.put("dimension", embedding.length);
        response.put("embedding", embedding);

        return response;
    }

    @PostMapping("/similarity")
    public Map<String, Object> similarity() {

        String sentenceA =
                "How can I reset my internet banking password?";

        String sentenceB =
                "Customers can change their online banking password using the Forgot Password option.";

        String sentenceC =
                "You can withdraw cash from any supported ATM.";

        String question =
                "I forgot my online banking password. How can I change it?";

        float[] questionVector =
                embeddingService.generateEmbedding(question);

        float[] vectorA =
                embeddingService.generateEmbedding(sentenceA);

        float[] vectorB =
                embeddingService.generateEmbedding(sentenceB);

        float[] vectorC =
                embeddingService.generateEmbedding(sentenceC);

        double similarityA =
                embeddingService.cosineSimilarity(
                        questionVector,
                        vectorA);

        double similarityB =
                embeddingService.cosineSimilarity(
                        questionVector,
                        vectorB);

        double similarityC =
                embeddingService.cosineSimilarity(
                        questionVector,
                        vectorC);

        Map<String, Object> response = new HashMap<>();

        response.put("question", question);
        response.put("sentenceA", similarityA);
        response.put("sentenceB", similarityB);
        response.put("sentenceC", similarityC);

        return response;
    }

    @PostMapping("/vector-store")
    public Map<String, String> saveToVectorStore(
            @RequestBody String text) {

        vectorStoreService.saveText(text);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Text saved successfully to vector store");

        return response;
    }

    @PostMapping("/search")
    public List<Document> search(
            @RequestBody String query) {

        return vectorStoreService.search(query);
    }
}