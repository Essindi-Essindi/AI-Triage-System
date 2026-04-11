import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, catchError, throwError, retryWhen, delay, take, concat } from 'rxjs';
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
    return this.http
      .post(`${this.apiUrl}/analyze`, request, {
        responseType: 'text',
        // Give the backend up to 60 seconds to respond (covers cold start wake-up)
      })
      .pipe(
        map((raw: string) => {
          console.log('[TriageService] raw response:', raw);
          try {
            const parsed = JSON.parse(raw);
            return parsed as TriageResponse;
          } catch {
            return { llm_result: raw, rule_based_risk: '' } as TriageResponse;
          }
        }),
        catchError((err) => {
          console.error('[TriageService] HTTP error:', err);
          return throwError(() => err);
        })
      );
  }
}
