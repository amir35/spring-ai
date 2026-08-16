package com.amir35.spring_ai.controller;

import com.amir35.spring_ai.dto.ChatRequest;
import com.amir35.spring_ai.dto.ChatResponse;
import com.amir35.spring_ai.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {

        String answer = chatService.ask(request.message());

        return new ChatResponse(answer);
    }
}