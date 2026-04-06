import { Component, ViewChild, ElementRef, AfterViewChecked, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TriageService, TriageRequest } from '../../services/triage';
import { FeedbackComponent } from '../feedback/feedback';

export interface ChatMessage {
  role: 'user' | 'assistant' | 'loading';
  bodyText?: string;
  riskLevel?: string;
  ruleRisk?: string;
  reference?: string;
  content: string;
  timestamp: Date;
  showFeedback?: boolean;
  feedbackDone?: boolean;
  id: number;
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, FeedbackComponent],
  templateUrl: './chat.html',
  styleUrls: ['./chat.scss']
})
export class ChatComponent implements AfterViewChecked {
  @ViewChild('messagesEnd') private messagesEnd!: ElementRef;
  @ViewChild('messagesArea') private messagesArea!: ElementRef;

  messages: ChatMessage[] = [];
  userInput = '';
  age: number = 25;
  medicalHistory = '';
  language = 'en';
  isLoading = false;
  msgCounter = 0;

  // Only scroll when a new message is added, not on every keystroke
  private shouldScrollToBottom = false;

  loadingPhrases = [
    '🩺 Consulting well-known medical references...',
    '📚 Reviewing clinical guidelines...',
    '🔍 Analysing your symptoms carefully...',
    '⏳ Please be patient, your response is coming...',
    '💊 Cross-checking with medical knowledge base...',
  ];
  currentLoadingPhrase = '';
  private phraseInterval: any;

  constructor(
    private triageService: TriageService,
    private cdr: ChangeDetectorRef
  ) {}

  ngAfterViewChecked() {
    // Only scroll the chat box when a new message arrives — never on keystrokes
    if (this.shouldScrollToBottom) {
      this.scrollChatToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  private scrollChatToBottom() {
    try {
      // Scroll only the messages area div, NOT the whole page
      const area = this.messagesArea?.nativeElement;
      if (area) {
        area.scrollTop = area.scrollHeight;
      }
    } catch {}
  }

  private cycleLoadingPhrases() {
    let i = 0;
    this.currentLoadingPhrase = this.loadingPhrases[0];
    this.phraseInterval = setInterval(() => {
      i = (i + 1) % this.loadingPhrases.length;
      this.currentLoadingPhrase = this.loadingPhrases[i];
    }, 2200);
  }

  private stopLoadingPhrases() {
    if (this.phraseInterval) { clearInterval(this.phraseInterval); this.phraseInterval = null; }
  }

  parseResponse(raw: string): { bodyText: string; riskLevel: string; reference: string } {
    const riskRegex = /^Risk level:\s*(low|medium|high)\s*$/im;
    const refRegex  = /^Reference:\s*(.+)$/im;

    let bodyText  = raw;
    let riskLevel = '';
    let reference = '';

    const refMatch = raw.match(refRegex);
    if (refMatch) {
      reference = refMatch[0].replace(/^Reference:\s*/i, '').trim();
      bodyText  = bodyText.replace(refMatch[0], '').trim();
    }

    const riskMatch = raw.match(riskRegex);
    if (riskMatch) {
      riskLevel = riskMatch[1].toLowerCase();
      bodyText  = bodyText.replace(riskMatch[0], '').trim();
    }

    bodyText = bodyText.replace(/\n{3,}/g, '\n\n').trim();
    return { bodyText, riskLevel, reference };
  }

  getRiskClass(risk: string): string {
    switch (risk?.toLowerCase()) {
      case 'high':   return 'risk-high';
      case 'medium': return 'risk-medium';
      case 'low':    return 'risk-low';
      default:       return 'risk-unknown';
    }
  }

  getRiskLabel(risk: string): string {
    switch (risk?.toLowerCase()) {
      case 'high':   return '🔴 HIGH RISK';
      case 'medium': return '🟠 MEDIUM RISK';
      case 'low':    return '🟢 LOW RISK';
      default:       return '⚪ RISK UNKNOWN';
    }
  }

  send() {
    const text = this.userInput.trim();
    if (!text || this.isLoading) return;

    this.messages.push({ role: 'user', content: text, timestamp: new Date(), id: this.msgCounter++ });

    const loadingId = this.msgCounter++;
    this.messages.push({ role: 'loading', content: '', timestamp: new Date(), id: loadingId });

    this.userInput = '';
    this.isLoading = true;
    this.shouldScrollToBottom = true; // scroll only now that a message was added
    this.cycleLoadingPhrases();

    const request: TriageRequest = {
      symptoms: text,
      age: this.age,
      medicalHistory: this.medicalHistory || 'none',
      language: this.language
    };

    this.triageService.analyze(request).subscribe({
      next: (res) => {
        this.stopLoadingPhrases();
        this.isLoading = false;
        this.messages = this.messages.filter(m => m.id !== loadingId);

        const raw = res.llm_result || '(no response received)';
        const { bodyText, riskLevel, reference } = this.parseResponse(raw);

        const assistantMsgId = this.msgCounter++;
        this.messages.push({
          role: 'assistant',
          content: raw,
          bodyText,
          riskLevel: riskLevel || res.rule_based_risk,
          ruleRisk: res.rule_based_risk,
          reference,
          timestamp: new Date(),
          showFeedback: false,
          feedbackDone: false,
          id: assistantMsgId
        });

        this.shouldScrollToBottom = true; // scroll when response arrives
        this.cdr.detectChanges();

        setTimeout(() => {
          const msg = this.messages.find(m => m.id === assistantMsgId);
          if (msg) { msg.showFeedback = true; this.cdr.detectChanges(); }
        }, 800);
      },

      error: (err) => {
        this.stopLoadingPhrases();
        this.isLoading = false;
        this.messages = this.messages.filter(m => m.id !== loadingId);

        const errMsg = err.status === 429
          ? '⚠️ The AI service is temporarily rate-limited. Please wait a moment and try again.'
          : `⚠️ Error ${err.status || ''}: Unable to reach the medical server. Check that the backend is running on localhost:8080.`;

        this.messages.push({ role: 'assistant', content: errMsg, timestamp: new Date(), id: this.msgCounter++ });
        this.shouldScrollToBottom = true;
        this.cdr.detectChanges();
      }
    });
  }

  onKeyDown(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); this.send(); }
  }

  onFeedbackDone(msgId: number) {
    const msg = this.messages.find(m => m.id === msgId);
    if (msg) { msg.feedbackDone = true; msg.showFeedback = false; this.cdr.detectChanges(); }
  }
}
