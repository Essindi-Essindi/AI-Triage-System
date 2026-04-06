import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChatComponent } from '../../components/chat/chat';
import { FeedbackService } from '../../services/feedback';

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
export class HomeComponent implements OnInit {
  darkMode = false;
  allFeedback: FeedbackEntry[] = [];

  constructor(private feedbackService: FeedbackService) {}

  ngOnInit() {
    // Always start at the top of the page
    window.scrollTo({ top: 0, left: 0, behavior: 'instant' });
    this.loadReviews();
  }

  loadReviews() {
    this.feedbackService.getAll().subscribe({
      next: (data: FeedbackEntry[]) => {
        this.allFeedback = data.filter((f: FeedbackEntry) => f.comment && f.comment.trim() !== '');
      },
      error: (err: unknown) => console.error('Could not load reviews:', err)
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
}
