package com.amir35.spring_ai;

import com.amir35.spring_ai.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public String chat(@RequestBody String message) {
        return chatService.ask(message);
    }
}