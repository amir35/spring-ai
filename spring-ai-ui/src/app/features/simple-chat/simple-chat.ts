import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { RagService } from '../../core/services/rag.service';
import { ChatService } from '../../core/services/chat-service';

@Component({
  selector: 'app-simple-chat',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './simple-chat.html',
  styleUrl: './simple-chat.css'
})
export class SimpleChatComponent {

  private readonly chatService = inject(ChatService);

  // QUESTION
  question = '';

  // ANSWER
  answer = signal('');

  // LOADING
  loading = signal(false);

  // STREAMING MODE
  isStreamingMode = false;

  // =========================================
  // ASK QUESTION
  // =========================================
  getAnswer(): void {

    if (!this.question.trim()) {
      return;
    }

    this.loading.set(true);

    this.answer.set('');

    if (this.isStreamingMode) {
      this.simpleChatStream();
    } else {
      this.simpleChat();
    }
  }


  // =========================================
  // NORMAL CHAT
  // =========================================
  private simpleChat(): void {

    this.chatService.simpleChat(this.question).subscribe({

        next: response => {
          console.log( 'Simple Chat Response:', response);
          this.answer.set(response.result.output.text);
          this.loading.set(false);
        },


        error: error => {
          console.error('Simple chat error:', error);
          this.answer.set('Something went wrong while generating the response.');
          this.loading.set(false);
        }

      });
  }


  // =========================================
  // STREAMING CHAT
  // =========================================

  private simpleChatStream(): void {

    this.chatService.simpleChatStream(this.question,

      // CHUNK
      (chunk: string) => {
        console.log('Simple Chat Chunk:', chunk);
        this.answer.update( current => current + chunk);
      },


      // COMPLETE
      () => {
        console.log('Simple Chat Streaming Completed');
        this.loading.set(false);
      },


      // ERROR
      (error: any) => {
        console.error('Simple chat streaming error:', error);
        this.answer.set('Something went wrong while generating the response.');
        this.loading.set(false);
      }
    );
  }

}