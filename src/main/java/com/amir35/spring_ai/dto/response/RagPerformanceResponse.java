package com.amir35.spring_ai.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RagPerformanceResponse {

    private Integer retrievedChunks;

    private Long responseTimeMs;

    private Integer contextTokens;

    private Long chatClientTimeMs;
}