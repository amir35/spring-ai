package com.amir35.spring_ai.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenUsageResponse {

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;
}