import { Component } from '@angular/core';

import { SimpleChatComponent } from '../features/simple-chat/simple-chat';
import { RagChatComponent } from '../features/rag-chat/rag-chat';
import { RagStreamComponent } from '../features/rag-stream/rag-stream';

@Component({
  selector: 'app-dashboard-component',
  standalone: true,
  imports: [
    SimpleChatComponent,
    RagChatComponent,
    RagStreamComponent
  ],
  templateUrl: './dashboard-component.html',
  styleUrl: './dashboard-component.css',
})
export class DashboardComponent {
  activeMenu: 'simple' | 'document' | 'stream' = 'simple';
}
