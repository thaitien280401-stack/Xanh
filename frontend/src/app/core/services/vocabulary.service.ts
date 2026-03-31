import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Vocabulary, Page } from '../models/vocabulary.model';

@Injectable({ providedIn: 'root' })
export class VocabularyService {
  private readonly apiUrl = `${environment.apiUrl}/vocabularies`;

  constructor(private http: HttpClient) {}

  getByTopic(topicId: string, page = 0, size = 20): Observable<Page<Vocabulary>> {
    return this.http.get<Page<Vocabulary>>(this.apiUrl, {
      params: new HttpParams().set('topicId', topicId).set('page', page).set('size', size)
    });
  }

  getById(id: string): Observable<Vocabulary> {
    return this.http.get<Vocabulary>(`${this.apiUrl}/${id}`);
  }

  search(q: string, page = 0, size = 20): Observable<Page<Vocabulary>> {
    return this.http.get<Page<Vocabulary>>(`${this.apiUrl}/search`, {
      params: new HttpParams().set('q', q).set('page', page).set('size', size)
    });
  }

  importWord(topicId: string, word: string): Observable<Vocabulary[]> {
    return this.http.post<Vocabulary[]>(`${this.apiUrl}/import`, null, {
      params: new HttpParams().set('topicId', topicId).set('word', word)
    });
  }
}
