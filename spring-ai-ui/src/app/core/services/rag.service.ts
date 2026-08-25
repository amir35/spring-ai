import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { RagResponse } from '../../models/rag-response';
import { ChatRequest } from '../../models/ask-request';

@Injectable({
  providedIn: 'root'
})
export class RagService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl = 'http://localhost:8080/api/ai';

  // =========================================================
  // RAG - NORMAL ASK
  // =========================================================
  askQuestion(question: string, conversationId: string): Observable<RagResponse> {

    console.log("Question: ", question);

    return this.http.post<RagResponse>(`${this.apiUrl}/ask`,
      {
        question: question,
        conversationId: conversationId
      }
    );
  }


  // =========================================================
  // RAG - STREAMING ASK
  // =========================================================

  askQuestionStream(question: string, conversationId: string,
    onChunk: (chunk: string) => void,
    onMetadata?: (metadata: RagResponse) => void,
    onComplete?: () => void,
    onError?: (error: any) => void
  ): void {

    const url = `${this.apiUrl}/ask/stream` +
      `?question=${encodeURIComponent(question)}` +
      `&conversationId=${encodeURIComponent(conversationId)}`;

    fetch(url,
      {
        method: 'POST',
        headers: {
          Accept: 'text/event-stream'
        }
      }).then(async response => {

        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`);
        }

        if (!response.body) {
          throw new Error('Streaming is not supported by this response.');
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();

        let buffer = '';
        while (true) {

          const { value, done } = await reader.read();

          if (done) {
            break;
          }

          buffer += decoder.decode(
            value,
            { stream: true }
          );

          /*
           * Normalize Windows CRLF
           */
          buffer = buffer.replace(/\r\n/g, '\n');
          const events = buffer.split('\n\n');

          /*
           * Last item may be incomplete.
           */
          buffer = events.pop() ?? '';

          for (const event of events) {
            if (event.trim()) {
              this.processSseEvent(event, onChunk, onMetadata);
            }
          }
        }

        /*
         * Process remaining data.
         */
        buffer = buffer.replace(/\r\n/g, '\n');

        if (buffer.trim()) {
          this.processSseEvent(buffer, onChunk, onMetadata);
        }

        console.log('Streaming completed');

        onComplete?.();
      })
      .catch(error => {
        console.error('Streaming error:', error);
        onError?.(error);
      });
  }

  private processSseEvent(event: string, onChunk: (chunk: string) => void,
    onMetadata?: (metadata: RagResponse) => void
  ): void {

    console.log('RAW SSE EVENT:', event);

    const lines = event.split('\n');

    let eventType = '';
    const dataLines: string[] = [];

    for (const line of lines) {

      if (line.startsWith('event:')) {

        eventType = line.substring(6).trim();

      } else if (line.startsWith('data:')) {

        /*
         * Don't trim the actual data.
         * Spaces/newlines can be part of the LLM response.
         */
        dataLines.push(line.substring(5)
        );
      }
    }

    const data = dataLines.join('\n');

    console.log('SSE TYPE:', eventType);
    console.log('SSE DATA:', data);


    // ==========================================
    // 1. Explicit chunk event
    // ==========================================

    if (eventType === 'chunk') {
      onChunk(data);
      return;
    }


    // ==========================================
    // 2. Metadata event
    // ==========================================

    if (eventType === 'metadata') {
      try {
        const metadata = JSON.parse(data) as RagResponse;

        console.log('METADATA RECEIVED:', metadata);

        onMetadata?.(metadata);

      } catch (error) {
        console.error('Metadata parsing failed:', error);
      }

      return;
    }


    // ==========================================
    // 3. IMPORTANT
    // Backend currently sends plain SSE data
    // without "event: chunk"
    // ==========================================

    if (!eventType && data) {
      console.log('Plain SSE chunk received:', data);
      onChunk(data);
      return;
    }


    console.warn('Unknown SSE event:', eventType, data);
  }

}