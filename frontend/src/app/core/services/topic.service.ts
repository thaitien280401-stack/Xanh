import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Topic, CreateTopicRequest } from '../models/topic.model';

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

  create(request: CreateTopicRequest): Observable<Topic> {
    return this.http.post<Topic>(this.apiUrl, request);
  }

  searchExternal(query: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/external/search`, { params: { query } });
  }
}
