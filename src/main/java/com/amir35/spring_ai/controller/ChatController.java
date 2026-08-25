package com.amir35.spring_ai.controller;

import com.amir35.spring_ai.dto.request.ChatRequest;
import com.amir35.spring_ai.service.ChatService;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:4200")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ChatResponse simpleChat(@RequestBody ChatRequest request) {

        System.out.println("Question : " +request.message());

        return chatService.simpleChat(request.message());
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<ServerSentEvent<String>> simpleChatStream(@RequestBody ChatRequest request) {

        System.out.println("Question : " +request.message());

        return chatService.simpleChatStream(request.message())
                .map(text -> ServerSentEvent
                                .builder(text)
                                .event("chunk")
                                .build()
                );
    }
}