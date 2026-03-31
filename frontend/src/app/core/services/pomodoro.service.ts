import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PomodoroSession, StartSessionRequest } from '../models/pomodoro.model';
import { Page } from '../models/vocabulary.model';

@Injectable({ providedIn: 'root' })
export class PomodoroService {
  private readonly apiUrl = `${environment.apiUrl}/pomodoro`;

  constructor(private http: HttpClient) {}

  startSession(request: StartSessionRequest): Observable<PomodoroSession> {
    return this.http.post<PomodoroSession>(`${this.apiUrl}/sessions`, request);
  }

  getSessions(page = 0, size = 10): Observable<Page<PomodoroSession>> {
    return this.http.get<Page<PomodoroSession>>(`${this.apiUrl}/sessions`, {
      params: { page, size }
    });
  }

  getSession(id: string): Observable<PomodoroSession> {
    return this.http.get<PomodoroSession>(`${this.apiUrl}/sessions/${id}`);
  }

  completeSession(id: string, wordsStudied: number): Observable<PomodoroSession> {
    return this.http.put<PomodoroSession>(`${this.apiUrl}/sessions/${id}/complete`, null, {
      params: { wordsStudied }
    });
  }

  abandonSession(id: string): Observable<PomodoroSession> {
    return this.http.put<PomodoroSession>(`${this.apiUrl}/sessions/${id}/abandon`, null);
  }
}
