import { Component, ViewChild, ElementRef, AfterViewChecked, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TriageService, TriageRequest } from '../../services/triage';
import { FeedbackComponent } from '../feedback/feedback';

export interface ChatMessage {
  role: 'user' | 'assistant' | 'loading';
  content: string;
  ruleRisk?: string;
  riskLevel?: string;
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

  messages: ChatMessage[] = [];
  userInput = '';
  age: number = 25;
  medicalHistory = '';
  language = 'en';
  isLoading = false;
  msgCounter = 0;

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
    this.scrollToBottom();
  }

  private scrollToBottom() {
    try {
      this.messagesEnd?.nativeElement.scrollIntoView({ behavior: 'smooth' });
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
    if (this.phraseInterval) {
      clearInterval(this.phraseInterval);
      this.phraseInterval = null;
    }
  }

  /**
   * Extracts the risk level from the LLM plain-text response.
   * The backend prompt asks the LLM to write "Risk level: high/medium/low"
   * (or the French equivalent "Niveau de risque: …").
   */
  extractRiskFromText(text: string): string {
    if (!text) return '';
    const lower = text.toLowerCase();

    // Match "risk level: high" / "niveau de risque : high" and variants
    const match = lower.match(/(?:risk level|niveau de risque)\s*[:–-]\s*(high|medium|low)/i);
    if (match) return match[1].toLowerCase();

    // Fallback keyword scan
    if (lower.includes('risk level: high')   || lower.includes('niveau de risque : high'))   return 'high';
    if (lower.includes('risk level: medium') || lower.includes('niveau de risque : medium')) return 'medium';
    if (lower.includes('risk level: low')    || lower.includes('niveau de risque : low'))    return 'low';

    // Broader fallback
    if (lower.includes('high risk'))   return 'high';
    if (lower.includes('medium risk')) return 'medium';
    if (lower.includes('low risk'))    return 'low';

    return '';
  }

  send() {
    const text = this.userInput.trim();
    if (!text || this.isLoading) return;

    // Add user bubble
    this.messages.push({
      role: 'user',
      content: text,
      timestamp: new Date(),
      id: this.msgCounter++
    });

    // Add loading bubble and hold a stable reference to it
    const loadingId = this.msgCounter++;
    const loadingMsg: ChatMessage = {
      role: 'loading',
      content: '',
      timestamp: new Date(),
      id: loadingId
    };
    this.messages.push(loadingMsg);

    this.userInput = '';
    this.isLoading = true;
    this.cycleLoadingPhrases();

    const request: TriageRequest = {
      symptoms: text,
      age: this.age,
      medicalHistory: this.medicalHistory || 'none',
      language: this.language
    };

    console.log('[Chat] Sending request:', request);

    this.triageService.analyze(request).subscribe({
      next: (res) => {
        console.log('[Chat] Got response:', res);

        this.stopLoadingPhrases();
        this.isLoading = false;

        // Remove loading bubble by its stable id — never fails silently
        this.messages = this.messages.filter(m => m.id !== loadingId);

        const llmText = res.llm_result || '(no response received)';
        const riskFromText = this.extractRiskFromText(llmText);

        const assistantMsgId = this.msgCounter++;
        this.messages.push({
          role: 'assistant',
          content: llmText,
          ruleRisk: res.rule_based_risk,
          riskLevel: riskFromText || res.rule_based_risk,
          timestamp: new Date(),
          showFeedback: false,
          feedbackDone: false,
          id: assistantMsgId
        });

        // Trigger change detection so Angular re-renders immediately
        this.cdr.detectChanges();

        // Show feedback widget after a short delay
        setTimeout(() => {
          const msg = this.messages.find(m => m.id === assistantMsgId);
          if (msg) {
            msg.showFeedback = true;
            this.cdr.detectChanges();
          }
        }, 800);
      },

      error: (err) => {
        console.error('[Chat] Error:', err);

        this.stopLoadingPhrases();
        this.isLoading = false;

        // Remove loading bubble
        this.messages = this.messages.filter(m => m.id !== loadingId);

        const errMsg = err.status === 429
          ? '⚠️ The AI service is temporarily rate-limited. Please wait a moment and try again.'
          : `⚠️ Error ${err.status || ''}: Unable to reach the medical server. Check that the backend is running on localhost:8080.`;

        this.messages.push({
          role: 'assistant',
          content: errMsg,
          timestamp: new Date(),
          id: this.msgCounter++
        });

        this.cdr.detectChanges();
      }
    });
  }

  onKeyDown(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  getRiskClass(risk: string): string {
    switch (risk?.toLowerCase()) {
      case 'high':   return 'risk-high';
      case 'medium': return 'risk-medium';
      case 'low':    return 'risk-low';
      default:       return 'risk-unknown';
    }
  }

  onFeedbackDone(msgId: number) {
    const msg = this.messages.find(m => m.id === msgId);
    if (msg) {
      msg.feedbackDone = true;
      msg.showFeedback = false;
      this.cdr.detectChanges();
    }
  }
}
