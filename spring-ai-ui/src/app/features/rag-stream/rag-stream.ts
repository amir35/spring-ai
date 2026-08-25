import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RagService } from '../../core/services/rag.service';
import { RagResponse } from '../../models/rag-response';

@Component({
  selector: 'app-rag-stream',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './rag-stream.html',
  styleUrl: './rag-stream.css',
})
export class RagStreamComponent {

  private readonly ragService = inject(RagService);

  // RESPONSE
  response = signal<RagResponse | null>(null);

  // QUESTION
  question = '';

  // CONVERSATION
  conversationId = 'conversation-1';

  // STREAMING STATE
  isStreaming = false;

  // ASK QUESTION STREAM
  askQuestionStream(): void {

    if (!this.question.trim()) {
      return;
    }

    this.isStreaming = true;

    this.response.set({
      question: this.question,
      answer: '',
      sources: [],
      performance: {
        retrievedChunks: 0,
        contextTokens: 0,
        responseTimeMs: 0,
        chatClientTimeMs: 0
      },
      tokenUsage: {
        promptTokens: 0,
        completionTokens: 0,
        totalTokens: 0
      }
    });




    this.ragService.askQuestionStream(this.question, this.conversationId,

      // CHUNK
      (chunk: string) => {
        console.log('Angular chunk:', chunk);

        this.response.update(current => {

          if (!current) {
            return current;
          }

          return {
            ...current,

            answer: current.answer + chunk
          };
        });
      },

      // METADATA
      (metadata: RagResponse) => {

        console.log('Angular metadata:', metadata);

        this.response.update(current => {

          if (!current) {
            return metadata;
          }

          return {
            ...current,

            answer: metadata.answer || current.answer,

            sources: metadata.sources,

            performance: metadata.performance,

            tokenUsage: metadata.tokenUsage
          };
        });
      },

      // COMPLETE
      () => {
        console.log('Streaming completed');
        this.isStreaming = false;
      },


      // ERROR
      (error: any) => {
        console.error('Streaming error:', error);
        this.isStreaming = false;
        this.response.update(current => {

          if (!current) {
            return current;
          }

          return {
            ...current,

            answer: 'Something went wrong while generating the response.'
          };
        });
      }
    );
  }
}