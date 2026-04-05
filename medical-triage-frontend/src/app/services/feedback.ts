import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface FeedbackRequest {
  rating: string;
  comment: string;
}

@Injectable({ providedIn: 'root' })
export class FeedbackService {
  private readonly apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  submit(feedback: FeedbackRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/feedback`, feedback, { responseType: 'text' });
  }
}
