package com.amir35.spring_ai.service;

import com.amir35.spring_ai.dto.response.RagPerformanceResponse;
import java.util.concurrent.atomic.AtomicReference;
import com.amir35.spring_ai.dto.response.RagResponse;
import com.amir35.spring_ai.dto.response.SourceResponse;
import com.amir35.spring_ai.dto.response.TokenUsageResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RagService {

    private final ChatClient chatClient;

    public RagService(ChatClient.Builder chatClientBuilder,VectorStore vectorStore,
            ChatMemory chatMemory) {

        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        // -----------------------------------------
                        // Chat Memory
                        // -----------------------------------------
                        MessageChatMemoryAdvisor
                                .builder(chatMemory)
                                .build(),
                        // -----------------------------------------
                        // RAG
                        // -----------------------------------------
                        QuestionAnswerAdvisor
                                .builder(vectorStore)
                                .searchRequest(
                                        SearchRequest.builder()
                                                .topK(2)
                                                .similarityThreshold(0.0)
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    // =========================================================
    // NORMAL / NON-STREAMING API
    // =========================================================
    public RagResponse askQuestion(String question, String conversationId) {

        long startTime = System.currentTimeMillis();

        long chatClientStart = System.currentTimeMillis();

        ChatClientResponse response = chatClient
                .prompt()
                .user(question)
                .advisors(advisor ->
                        advisor.param(
                                ChatMemory.CONVERSATION_ID,
                                conversationId
                        )
                )
                .call()
                .chatClientResponse();

        long chatClientTime =  System.currentTimeMillis() - chatClientStart;

        long responseTime =  System.currentTimeMillis() - startTime;

        // -----------------------------------------
        // Answer
        // -----------------------------------------
        String answer = extractAnswer(response);

        // -----------------------------------------
        // Retrieved documents
        // -----------------------------------------
        List<Document> documents = extractDocuments(response);

        // -----------------------------------------
        // Sources
        // -----------------------------------------
        List<SourceResponse> sources = extractSources(documents);

        // -----------------------------------------
        // Token usage
        // -----------------------------------------
        TokenUsageResponse tokenUsage = extractTokenUsage(response);

        // -----------------------------------------
        // Context tokens
        // -----------------------------------------
        Integer contextTokens = estimateContextTokens(documents);

        // -----------------------------------------
        // Performance
        // -----------------------------------------
        RagPerformanceResponse performance = RagPerformanceResponse.builder()
                        .retrievedChunks(documents.size())
                        .contextTokens(contextTokens)
                        .responseTimeMs(responseTime)
                        .chatClientTimeMs(chatClientTime)
                        .build();

        // -----------------------------------------
        // Final response
        // -----------------------------------------
        return RagResponse.builder()
                .question(question)
                .answer(answer)
                .sources(sources)
                .tokenUsage(tokenUsage)
                .performance(performance)
                .build();
    }


    // =========================================================
    // STREAMING API
    // =========================================================
    public Flux<ServerSentEvent<?>> askQuestionStream(
            String question,
            String conversationId) {

        long startTime = System.currentTimeMillis();

        StringBuilder fullAnswer = new StringBuilder();

        AtomicReference<ChatClientResponse> lastResponse =
                new AtomicReference<>();


        Flux<ChatClientResponse> responseFlux = chatClient
                        .prompt()
                        .user(question)
                        .advisors(advisor ->
                                advisor.param(
                                        ChatMemory.CONVERSATION_ID,
                                        conversationId
                                )
                        )
                        .stream()
                        .chatClientResponse();


        /*
         * Stream each response immediately.
         */
        Flux<ServerSentEvent<?>> chunks = responseFlux
                        .doOnNext(lastResponse::set)
                        .map(this::extractAnswer)
                        .filter(text ->
                                text != null && !text.isEmpty()
                        )
                        .doOnNext(fullAnswer::append)
                        .map(text -> ServerSentEvent
                                        .builder()
                                        .event("chunk")
                                        .data(text)
                                        .build()
                        );
        /*
         * Send metadata only after streaming finishes.
         */
        Flux<ServerSentEvent<?>> metadata = Flux.defer(() -> {

                    long responseTime = System.currentTimeMillis() - startTime;

                    ChatClientResponse finalResponse = lastResponse.get();

                    List<Document> documents = extractDocuments(finalResponse);

                    List<SourceResponse> sources = extractSources(documents);

                    TokenUsageResponse tokenUsage = extractTokenUsage(finalResponse);

                    Integer contextTokens = estimateContextTokens(documents);

                    RagPerformanceResponse performance = RagPerformanceResponse.builder()
                                    .retrievedChunks(documents.size())
                                    .contextTokens(contextTokens)
                                    .responseTimeMs(responseTime)
                                    .chatClientTimeMs(responseTime)
                                    .build();


                    RagResponse ragResponse = RagResponse.builder()
                                    .question(question)
                                    .answer(fullAnswer.toString())
                                    .sources(sources)
                                    .tokenUsage(tokenUsage)
                                    .performance(performance)
                                    .build();


                    return Flux.just(ServerSentEvent
                                    .builder()
                                    .event("metadata")
                                    .data(ragResponse)
                                    .build()
                    );
                });


        return chunks.concatWith(metadata);
    }


    // =========================================================
    // EXTRACT ANSWER
    // =========================================================
    private String extractAnswer(ChatClientResponse response) {

        if (response == null || response.chatResponse() == null
                || response.chatResponse().getResult() == null
                || response.chatResponse().getResult().getOutput() == null) {
            return "";
        }

        String text = response.chatResponse()
                        .getResult()
                        .getOutput()
                        .getText();
        return text != null ? text : "";
    }


    // =========================================================
    // EXTRACT DOCUMENTS
    // =========================================================

    private List<Document> extractDocuments(ChatClientResponse response) {

        if (response == null || response.context() == null) {
            return List.of();
        }

        Object retrievedDocuments = response.context()
                        .get( QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);

        if (retrievedDocuments instanceof List<?> list) {

            return list.stream()
                    .filter(Document.class::isInstance)
                    .map(Document.class::cast)
                    .toList();
        }
        return List.of();
    }


    // =========================================================
    // SOURCES
    // =========================================================
    private List<SourceResponse> extractSources( List<Document> documents) {

        if (documents == null || documents.isEmpty()) {
            return List.of();
        }

        return documents.stream()
                .map(this::toSourceResponse)
                .distinct()
                .toList();
    }


    private SourceResponse toSourceResponse(Document document) {

        Map<String, Object> metadata = document.getMetadata();

        String fileName = (String) metadata.get("file_name");

        Integer pageNumber = getPageNumber(metadata);

        return SourceResponse.builder()
                .fileName(fileName)
                .pageNumber(pageNumber)
                .build();
    }


    private Integer getPageNumber(Map<String, Object> metadata) {

        Object pageNumber = metadata.get("page_number");

        if (pageNumber instanceof Number number) {
            return number.intValue();
        }
        return null;
    }


    // =========================================================
    // TOKEN USAGE
    // =========================================================
    private TokenUsageResponse extractTokenUsage(ChatClientResponse response) {

        if (response == null  || response.chatResponse() == null) {
            return TokenUsageResponse.builder().build();
        }

        Usage usage = response.chatResponse()
                        .getMetadata()
                        .getUsage();

        if (usage == null) {
            return TokenUsageResponse.builder()
                    .build();
        }

        return TokenUsageResponse.builder()
                .promptTokens(usage.getPromptTokens())
                .completionTokens(usage.getCompletionTokens())
                .totalTokens(usage.getTotalTokens())
                .build();
    }


    // =========================================================
    // CONTEXT TOKEN ESTIMATION
    // =========================================================
    private Integer estimateContextTokens(List<Document> documents) {

        if (documents == null || documents.isEmpty()) {
            return 0;
        }

        int characterCount = documents.stream()
                        .map(Document::getText)
                        .filter(text -> text != null)
                        .mapToInt(String::length)
                        .sum();

        /*
         * Rough English approximation:
         *
         * 1 token ≈ 4 characters
         *
         * This is NOT the exact model tokenizer.
         */
        return (characterCount + 3) / 4;
    }
}