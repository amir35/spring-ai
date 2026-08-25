package com.amir35.spring_ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public ChatResponse simpleChat(String message) {
        return chatClient
                .prompt()
                .system("""
                    You are an expert Java and Spring Boot assistant.

                    Answer questions clearly and accurately.

                    Explain technical concepts in simple terms.

                    Do not make up information.
                    """)
                .user(message)
                .call()
                .chatResponse();
    }

    public Flux<String> simpleChatStream(String question) {

        return chatClient
                .prompt()
                .system("""
                You are an expert Java and Spring Boot assistant.

                Answer questions clearly and accurately.

                Explain technical concepts in simple terms.

                Do not make up information.
                """)
                .user(question)
                .stream()
                .content();
    }
}
