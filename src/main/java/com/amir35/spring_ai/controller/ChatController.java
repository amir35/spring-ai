package com.amir35.spring_ai.controller;

import com.amir35.spring_ai.dto.request.ChatRequest;
import com.amir35.spring_ai.service.ChatService;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:4200")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {

        System.out.println("Question : " +request.message());

        return chatService.ask(request.message());
    }
}