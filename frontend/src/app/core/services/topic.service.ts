import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Topic, TopicPage, TopicStatus, CreateTopicRequest, PomodoroSyncRequest, PomodoroSyncResponse } from '../models/topic.model';

@Injectable({ providedIn: 'root' })
export class TopicService {
  private readonly apiUrl = `${environment.apiUrl}/topics`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Topic[]> {
    return this.http.get<Topic[]>(this.apiUrl);
  }

  getById(id: string): Observable<Topic> {
    return this.http.get<Topic>(`${this.apiUrl}/${id}`);
  }

  getPaged(status: TopicStatus = 'ACTIVE', page = 0, size = 18): Observable<TopicPage> {
    return this.http.get<TopicPage>(`${this.apiUrl}/paged`, {
      params: { status, page: page.toString(), size: size.toString() }
    });
  }

  create(request: CreateTopicRequest): Observable<Topic> {
    return this.http.post<Topic>(this.apiUrl, request);
  }

  updateStatus(id: string, status: TopicStatus): Observable<Topic> {
    return this.http.patch<Topic>(`${this.apiUrl}/${id}/status`, null, { params: { status } });
  }

  searchExternal(query: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/external/search`, { params: { query } });
  }

  pomodoroSync(keywords: string[]): Observable<PomodoroSyncResponse> {
    const body: PomodoroSyncRequest = { keywords };
    return this.http.post<PomodoroSyncResponse>(`${this.apiUrl}/pomodoro-sync`, body);
  }
}
