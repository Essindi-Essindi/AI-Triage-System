import { Component, ViewChild, ElementRef, AfterViewChecked, ChangeDetectorRef, NgZone, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TriageService, TriageRequest } from '../../services/triage';
import { FeedbackComponent } from '../feedback/feedback';

export interface ChatMessage {
  role: 'user' | 'assistant' | 'loading';
  bodyText?: string;
  riskLevel?: string;
  ruleRisk?: string;
  references?: string[];
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

  // Bubbles up to HomeComponent so it can immediately refresh the reviews section
  @Output() feedbackAdded = new EventEmitter<void>();

  messages: ChatMessage[] = [];
  userInput = '';
  age: number = 25;
  medicalHistory = '';
  language = 'en';
  isLoading = false;
  msgCounter = 0;

  private shouldScrollToBottom = false;

  loadingPhrases = [
    '🕛 Your diagnostics will be yours in a minute...',
    '🩺 Consulting well-known medical references...',
    '📚 Reviewing clinical guidelines...',
    '🔍 Analysing your symptoms carefully...',
    '⏳ Please be patient, your response is coming...',
    '💊 Cross-checking with medical knowledge base...',
    '🧹 Wrapping things up...........',
    '🏃🏽‍♂️ Almost there.....',
  ];
  currentLoadingPhrase = '';
  private phraseInterval: any;

  constructor(
    private triageService: TriageService,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone
  ) {}

  ngAfterViewChecked() {
    if (this.shouldScrollToBottom) {
      this.scrollChatToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  private scrollChatToBottom() {
    try {
      const area = this.messagesArea?.nativeElement;
      if (area) { area.scrollTop = area.scrollHeight; }
    } catch {}
  }

  private cycleLoadingPhrases() {
    let i = 0;
    this.ngZone.run(() => {
      this.currentLoadingPhrase = this.loadingPhrases[0];
      this.cdr.detectChanges();
    });
    this.phraseInterval = setInterval(() => {
      this.ngZone.run(() => {
        i = (i + 1) % this.loadingPhrases.length;
        this.currentLoadingPhrase = this.loadingPhrases[i];
        this.cdr.detectChanges();
      });
    }, 2200);
  }

  private stopLoadingPhrases() {
    if (this.phraseInterval) { clearInterval(this.phraseInterval); this.phraseInterval = null; }
  }

  parseResponse(raw: string): { bodyText: string; riskLevel: string; references: string[] } {
    const riskRegex = /^Risk level:\s*(low|medium|high)\s*$/im;
    const refLineRegex = /^Reference:\s*(.+)$/gim;

    let bodyText = raw;
    let riskLevel = '';
    const references: string[] = [];

    let refMatch;
    while ((refMatch = refLineRegex.exec(raw)) !== null) {
      const refValue = refMatch[1].trim();
      if (refValue) references.push(refValue);
      bodyText = bodyText.replace(refMatch[0], '');
    }

    const riskMatch = raw.match(riskRegex);
    if (riskMatch) {
      riskLevel = riskMatch[1].toLowerCase();
      bodyText = bodyText.replace(riskMatch[0], '');
    }

    bodyText = bodyText.replace(/\n{3,}/g, '\n\n').trim();
    return { bodyText, riskLevel, references };
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
    this.shouldScrollToBottom = true;
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
        this.ngZone.run(() => {
          this.isLoading = false;
          this.messages = this.messages.filter(m => m.id !== loadingId);

          const raw = res.llm_result || '(no response received)';
          const { bodyText, riskLevel, references } = this.parseResponse(raw);

          const assistantMsgId = this.msgCounter++;
          this.messages.push({
            role: 'assistant',
            content: raw,
            bodyText,
            riskLevel: riskLevel || res.rule_based_risk,
            ruleRisk: res.rule_based_risk,
            references,
            timestamp: new Date(),
            showFeedback: false,
            feedbackDone: false,
            id: assistantMsgId
          });

          this.shouldScrollToBottom = true;
          this.cdr.detectChanges();

          setTimeout(() => {
            const msg = this.messages.find(m => m.id === assistantMsgId);
            if (msg) { msg.showFeedback = true; this.cdr.detectChanges(); }
          }, 800);
        });
      },

      error: (err) => {
        this.stopLoadingPhrases();
        this.ngZone.run(() => {
          this.isLoading = false;
          this.messages = this.messages.filter(m => m.id !== loadingId);

          const errMsg = err.status === 429
            ? '⚠️ The AI service is temporarily rate-limited. Please wait a moment and try again.'
            : `⚠️ Error ${err.status || ''}: Unable to reach the medical server. Please try later.`;

          this.messages.push({ role: 'assistant', content: errMsg, timestamp: new Date(), id: this.msgCounter++ });
          this.shouldScrollToBottom = true;
          this.cdr.detectChanges();
        });
      }
    });
  }

  onKeyDown(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); this.send(); }
  }

  onFeedbackDone(msgId: number) {
    const msg = this.messages.find(m => m.id === msgId);
    if (msg) {
      msg.feedbackDone = true;
      msg.showFeedback = false;
      this.cdr.detectChanges();
      // Notify home to immediately reload the reviews section
      this.feedbackAdded.emit();
    }
  }
}
