package com.amir35.spring_ai.controller;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
class AiCompareController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    AiCompareController(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    @PostMapping("/compare")
    CompareResponse compare(@RequestBody QuestionRequest request) {
        String plainAnswer = this.chatClient.prompt()
                .system("You are helpful, but you do not have access to the private course handbook.")
                .user(request.question())
                .call()
                .content();

        QuestionAnswerAdvisor ragAdvisor = QuestionAnswerAdvisor.builder(this.vectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(3)
                        .similarityThreshold(0.50)
                        .build())
                .build();

        String ragAnswer = this.chatClient.prompt()
                .system("""
                        You are a beginner-friendly Spring AI tutor.
                        Answer using the retrieved course handbook context.
                        If the context does not contain the answer, say you do not know.
                        """)
                .advisors(ragAdvisor)
                .user(request.question())
                .call()
                .content();

        List<SourceSnippet> sources = this.vectorStore
                .similaritySearch(SearchRequest.builder()
                        .query(request.question())
                        .topK(3)
                        .similarityThreshold(0.50)
                        .build())
                .stream()
                .map(SourceSnippet::from)
                .toList();

        return new CompareResponse(request.question(), plainAnswer, ragAnswer, sources);
    }

    record QuestionRequest(String question) {
    }

    record CompareResponse(String question, String plainAnswer, String ragAnswer, List<SourceSnippet> sources) {
    }

    record SourceSnippet(String text, Map<String, Object> metadata) {
        static SourceSnippet from(Document document) {
            return new SourceSnippet(document.getText(), document.getMetadata());
        }
    }
}
