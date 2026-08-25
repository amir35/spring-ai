import { SourceResponse } from './source-response';

export interface RagResponse {

  question: string;

  answer: string;

  sources: SourceResponse[];

  tokenUsage: TokenUsageResponse;

  performance: RagPerformanceResponse;
}


export interface TokenUsageResponse {

  promptTokens: number;

  completionTokens: number;

  totalTokens: number;
}


export interface RagPerformanceResponse {

  retrievedChunks: number;

  responseTimeMs: number;

  contextTokens: number;
  chatClientTimeMs: number;
}