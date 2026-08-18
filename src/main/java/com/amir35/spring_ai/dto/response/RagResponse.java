package com.amir35.spring_ai.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RagResponse {

    private String question;

    private String answer;

    private List<SourceResponse> sources;

    private TokenUsageResponse tokenUsage;

    private RagPerformanceResponse performance;
}