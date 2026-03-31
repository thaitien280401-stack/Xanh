import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Quiz, GenerateQuizRequest, SubmitQuizRequest } from '../models/quiz.model';
import { Page } from '../models/vocabulary.model';

@Injectable({ providedIn: 'root' })
export class QuizService {
  private readonly apiUrl = `${environment.apiUrl}/quizzes`;

  constructor(private http: HttpClient) {}

  generate(request: GenerateQuizRequest): Observable<Quiz> {
    return this.http.post<Quiz>(`${this.apiUrl}/generate`, request);
  }

  submit(id: string, request: SubmitQuizRequest): Observable<Quiz> {
    return this.http.post<Quiz>(`${this.apiUrl}/${id}/submit`, request);
  }

  list(page = 0, size = 10): Observable<Page<Quiz>> {
    return this.http.get<Page<Quiz>>(this.apiUrl, { params: { page, size } });
  }

  getById(id: string): Observable<Quiz> {
    return this.http.get<Quiz>(`${this.apiUrl}/${id}`);
  }
}
