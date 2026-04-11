import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, retry, delay } from 'rxjs';
import { environment } from '../../environments/environment';

export interface FeedbackRequest {
  rating: string;
  comment: string;
}

export interface FeedbackEntry {
  id: number;
  rating: string;
  comment: string;
}

@Injectable({ providedIn: 'root' })
export class FeedbackService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  submit(feedback: FeedbackRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/feedback`, feedback, { responseType: 'text' });
  }

  getAll(): Observable<FeedbackEntry[]> {
    return this.http
      .get<FeedbackEntry[]>(`${this.apiUrl}/feedback/all`)
      .pipe(
        // Retry up to 4 times with 5s delay between attempts
        // This covers the full Render cold start window (up to ~30s)
        retry({ count: 4, delay: 5000 })
      );
  }
}
