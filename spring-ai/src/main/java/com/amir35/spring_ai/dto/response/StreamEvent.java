package com.amir35.spring_ai.dto.response;

public record StreamEvent(
        String type,
        Object data
) {
}