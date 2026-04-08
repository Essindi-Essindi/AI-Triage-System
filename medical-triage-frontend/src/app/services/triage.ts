import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';

export interface TriageRequest {
  symptoms: string;
  age: number;
  medicalHistory: string;
  language: string;
}

export interface TriageResponse {
  llm_result: string;
  rule_based_risk: string;
}

@Injectable({ providedIn: 'root' })
export class TriageService {
  private readonly apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  analyze(request: TriageRequest): Observable<TriageResponse> {
    // Fetch as plain text so Angular never fails on Content-Type mismatch
    return this.http
      .post(`${this.apiUrl}/analyze`, request, { responseType: 'text' })
      .pipe(
        map((raw: string) => {
          console.log('[TriageService] raw response:', raw);
          try {
            // Backend returns JSON string → parse it
            const parsed = JSON.parse(raw);
            return parsed as TriageResponse;
          } catch {
            // Backend returned plain text directly (not wrapped in JSON)
            return {
              llm_result: raw,
              rule_based_risk: ''
            } as TriageResponse;
          }
        }),
        catchError((err) => {
          console.error('[TriageService] HTTP error:', err);
          return throwError(() => err);
        })
      );
  }
}
