package com.amir35.spring_ai.controller;

import com.amir35.spring_ai.service.SimilarityService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class SimilarityController {

    private final SimilarityService similarityService;

    public SimilarityController(
            SimilarityService similarityService) {

        this.similarityService = similarityService;
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
                similarityService.generateEmbedding(question);

        float[] vectorA =
                similarityService.generateEmbedding(sentenceA);

        float[] vectorB =
                similarityService.generateEmbedding(sentenceB);

        float[] vectorC =
                similarityService.generateEmbedding(sentenceC);

        double similarityA =
                similarityService.cosineSimilarity(
                        questionVector,
                        vectorA);

        double similarityB =
                similarityService.cosineSimilarity(
                        questionVector,
                        vectorB);

        double similarityC =
                similarityService.cosineSimilarity(
                        questionVector,
                        vectorC);

        Map<String, Object> response = new HashMap<>();

        response.put("question", question);
        response.put("sentenceA", similarityA);
        response.put("sentenceB", similarityB);
        response.put("sentenceC", similarityC);

        return response;
    }
}