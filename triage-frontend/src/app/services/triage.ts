import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TriageService {

  private baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  analyze(data: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/analyze`, data);
  }

  submitFeedback(rating: string, comment: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/feedback`, { rating, comment },
      { responseType: 'text' });
  }
}
