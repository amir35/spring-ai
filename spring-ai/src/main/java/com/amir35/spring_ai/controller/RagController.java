package com.amir35.spring_ai.controller;

import com.amir35.spring_ai.dto.request.AskRequest;
import com.amir35.spring_ai.dto.response.RagResponse;
import com.amir35.spring_ai.service.RagService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:4200")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    // NON-STREAMING API
    @PostMapping("/ask")
    public RagResponse ask(@RequestBody AskRequest request) {

        return ragService.askQuestion(request.getQuestion(),request.getConversationId());
    }

    // STREAMING API
    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<?>> askQuestionStream(@RequestParam String question, @RequestParam String conversationId) {

        return ragService.askQuestionStream(question, conversationId);
    }
}