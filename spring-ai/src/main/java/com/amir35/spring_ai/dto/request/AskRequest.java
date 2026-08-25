package com.amir35.spring_ai.dto.request;

import lombok.Data;

@Data
public class AskRequest {

    private String question;
    private String conversationId;
}