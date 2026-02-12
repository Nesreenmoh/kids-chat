import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatService } from '../../services/chat.service';
import { FormsModule } from '@angular/forms';
import { askRequest } from '../../models/chat';


interface ChatMessage {
  role: 'user' | 'bot';
  text: string;
}


@Component({
  selector: 'app-chatbot',
  imports: [FormsModule, CommonModule],
  standalone: true,
  templateUrl: './chatbot.html',
  styleUrls: ['./chatbot.scss'],
})
export class ChatbotComponent {
messages: ChatMessage[] = [];
input: string = '';
sending = false;
  // NEW: toggle state
ragEnabled = false;


constructor(private chatService: ChatService,  private cdr: ChangeDetectorRef) {}

async onSend() {
  const question = this.input.trim();

  if (!question || this.sending) {
    return;
  };
  this.sending = true;
  this.messages.push({ role: 'user', text: question });
  this.input = '';


// Build the request including the toggle value
const req: askRequest = {
      question,
      useRag: this.ragEnabled
  };

this.chatService.askQuestion(req).subscribe({
  next: (response) => {
    const answer = response?.answer ?? "Sorry, I couldn't find an answer.";
    console.log('Bot response:', response);
    this.messages.push({ role: 'bot', text: answer });
    this.cdr.detectChanges();
  },
  error: (err) => {
    this.messages.push({
      role: 'bot',
      text: 'There was an error reaching the knowledge base. Please try again.'
    });
    this.cdr.detectChanges();
    console.error('Error fetching response:', err);
  },
  complete: () => {
    this.sending = false;
    this.cdr.detectChanges();
  }
});
}


onEnter(e: KeyboardEvent) {
    if (e.key === 'Enter') {
      e.preventDefault();
      this.onSend();
    }
  }
}
