import { Component } from '@angular/core';
import { TriageService } from '../../services/triage';

@Component({
  selector: 'app-triage',
  templateUrl: './triage.component.html',
  styleUrls: ['./triage.component.css']
})
export class TriageComponent {

  symptoms: string = '';
  age: number | null = null;
  medicalHistory: string = '';
  language: string = 'en';

  llmResult: any = null;
  ruleRisk: string = '';
  loading: boolean = false;
  error: string = '';

  feedbackRating: string = '';
  feedbackComment: string = '';
  feedbackSent: boolean = false;

  constructor(private triageService: TriageService) {}

  analyze() {
    if (!this.symptoms.trim()) return;
    this.loading = true;
    this.error = '';
    this.llmResult = null;
    this.ruleRisk = '';
    this.feedbackSent = false;

    const payload = {
      symptoms: this.symptoms,
      age: this.age,
      medicalHistory: this.medicalHistory,
      language: this.language
    };

    this.triageService.analyze(payload).subscribe({
      next: (data) => {
        this.ruleRisk = data.rule_based_risk;
        const raw = data.llm_result;
        try {
          this.llmResult = typeof raw === 'string' ? JSON.parse(raw) : raw;
        } catch {
          this.llmResult = { risk_level: 'unknown', symptoms: [], recommendations: [raw] };
        }
        this.loading = false;
      },
      error: () => {
        this.error = 'Something went wrong. Please try again.';
        this.loading = false;
      }
    });
  }

  submitFeedback() {
    this.triageService.submitFeedback(this.feedbackRating, this.feedbackComment)
      .subscribe(() => { this.feedbackSent = true; });
  }
}
