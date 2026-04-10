import { Component, OnInit, OnDestroy, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatComponent } from '../../components/chat/chat';
import { FeedbackService } from '../../services/feedback';
import { interval, Subscription } from 'rxjs';
import { switchMap, startWith } from 'rxjs/operators';

interface FeedbackEntry {
  id: number;
  rating: string;
  comment: string;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, ChatComponent],
  templateUrl: './home.html',
  styleUrls: ['./home.scss']
})
export class HomeComponent implements OnInit, OnDestroy {
  darkMode = false;
  allFeedback: FeedbackEntry[] = [];

  private pollSub?: Subscription;
  private readonly POLL_INTERVAL_MS = 10000; // refresh every 10 seconds
  private readonly MAX_RETRIES = 5;
  private retryCount = 0;

  constructor(
    private feedbackService: FeedbackService,
    private cdr: ChangeDetectorRef,
    private ngZone: NgZone
  ) {}

  ngOnInit() {
    window.scrollTo({ top: 0, left: 0, behavior: 'instant' });
    this.startPolling();
  }

  ngOnDestroy() {
    this.stopPolling();
  }

  /**
   * Polls /api/feedback/all every 10s.
   * startWith(0) fires immediately on subscription (no waiting for first tick).
   * Retries up to MAX_RETRIES times on error (handles Render cold start).
   * Runs inside NgZone so Angular detects every update.
   */
  private startPolling() {
    this.stopPolling(); // prevent duplicate subscriptions

    this.pollSub = interval(this.POLL_INTERVAL_MS)
      .pipe(
        startWith(0),
        switchMap(() => this.feedbackService.getAll())
      )
      .subscribe({
        next: (data: FeedbackEntry[]) => {
          this.ngZone.run(() => {
            this.retryCount = 0;
            this.allFeedback = data.filter(
              (f: FeedbackEntry) => f.comment && f.comment.trim() !== ''
            );
            this.cdr.detectChanges();
          });
        },
        error: (err: unknown) => {
          this.retryCount++;
          console.warn(`Could not load reviews (attempt ${this.retryCount}):`, err);
          this.stopPolling();

          // Retry with delay — handles Render free tier cold start (backend waking up)
          if (this.retryCount <= this.MAX_RETRIES) {
            setTimeout(() => this.startPolling(), 4000 * this.retryCount);
          }
        }
      });
  }

  private stopPolling() {
    if (this.pollSub) {
      this.pollSub.unsubscribe();
      this.pollSub = undefined;
    }
  }

  /**
   * Call this after a new feedback is submitted to force an immediate
   * refresh instead of waiting for the next 10s poll tick.
   */
  refreshFeedbackNow() {
    this.feedbackService.getAll().subscribe({
      next: (data: FeedbackEntry[]) => {
        this.ngZone.run(() => {
          this.allFeedback = data.filter(
            (f: FeedbackEntry) => f.comment && f.comment.trim() !== ''
          );
          this.cdr.detectChanges();
        });
      },
      error: (err: unknown) => console.error('Immediate refresh failed:', err)
    });
  }

  toggleTheme() {
    this.darkMode = !this.darkMode;
    document.documentElement.setAttribute('data-theme', this.darkMode ? 'dark' : 'light');
  }

  scrollToChat(event: Event) {
    event.preventDefault();
    document.getElementById('chat-section')?.scrollIntoView({ behavior: 'smooth' });
  }

  scrollToAbout(event: Event) {
    event.preventDefault();
    document.getElementById('about')?.scrollIntoView({ behavior: 'smooth' });
  }
}
