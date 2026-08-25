package com.amir35.spring_ai.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RagStreamEvent {

    private String type;

    private String content;

    private RagPerformanceResponse performance;

    private List<SourceResponse> sources;

    private TokenUsageResponse tokenUsage;
}