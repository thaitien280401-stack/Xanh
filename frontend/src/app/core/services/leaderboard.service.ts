import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LeaderboardEntry, ProgressStats } from '../models/leaderboard.model';

@Injectable({ providedIn: 'root' })
export class LeaderboardService {
  private readonly leaderboardUrl = `${environment.apiUrl}/leaderboard`;
  private readonly progressUrl = `${environment.apiUrl}/progress`;

  constructor(private http: HttpClient) {}

  getLeaderboard(type: 'weekly' | 'all-time' = 'all-time', limit = 20): Observable<LeaderboardEntry[]> {
    return this.http.get<LeaderboardEntry[]>(this.leaderboardUrl, { params: { type, limit } });
  }

  getMyRank(): Observable<LeaderboardEntry> {
    return this.http.get<LeaderboardEntry>(`${this.leaderboardUrl}/me`);
  }

  getStats(): Observable<ProgressStats> {
    return this.http.get<ProgressStats>(`${this.progressUrl}/stats`);
  }
}
