import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FeedbackService } from '../../services/feedback';

@Component({
  selector: 'app-feedback',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './feedback.html',
  styleUrls: ['./feedback.scss']
})
export class FeedbackComponent {
  @Input() msgId!: number;
  @Output() feedbackSubmitted = new EventEmitter<void>();

  rating: 'useful' | 'not useful' | null = null;
  comment = '';
  submitting = false;
  error = '';

  constructor(private feedbackService: FeedbackService) {}

  select(r: 'useful' | 'not useful') {
    this.rating = r;
    this.error = '';
  }

  submit() {
    if (!this.rating) { this.error = 'Please select a rating first.'; return; }
    this.submitting = true;
    this.feedbackService.submit({ rating: this.rating, comment: this.comment }).subscribe({
      next: () => {
        this.submitting = false;
        // Emit to chat, which bubbles up to home to trigger immediate refresh
        this.feedbackSubmitted.emit();
      },
      error: () => {
        this.submitting = false;
        this.error = 'Could not send feedback. Try again.';
      }
    });
  }
}
