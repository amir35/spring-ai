import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RagService } from '../../core/services/rag.service';
import { RagResponse } from '../../models/rag-response';

@Component({
  selector: 'app-rag-chat',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './rag-chat.html',
  styleUrl: './rag-chat.css'
})
export class RagChatComponent {

  private readonly ragService = inject(RagService);

  // RESPONSE
  ragResponse = signal<RagResponse | null>(null);

  // QUESTION
  question = '';

  // CONVERSATION
  conversationId = 'conversation-1';

  loading = signal(false);


  // ASK QUESTION
  askQuestion(): void {

    if (!this.question.trim()) {
      return;
    }

    this.loading.set(true);

    this.ragResponse.set({
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

    this.ragService.askQuestion(this.question, this.conversationId).subscribe({
      next: response => {
          this.ragResponse.set(response);
          this.loading.set(false);
        },


        error: error => {
          console.error('Simple chat error:', error );
          //this.ragResponse.set('Something went wrong while generating the response.');
          this.loading.set(false);
        }
    });
  }

}