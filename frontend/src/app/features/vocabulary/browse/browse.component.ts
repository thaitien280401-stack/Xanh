import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatBadgeModule } from '@angular/material/badge';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { debounceTime, Subject, switchMap, of } from 'rxjs';
import { TopicService } from '../../../core/services/topic.service';
import { VocabularyService } from '../../../core/services/vocabulary.service';
import { Topic } from '../../../core/models/topic.model';
import { Vocabulary, Page } from '../../../core/models/vocabulary.model';

@Component({
  selector: 'app-browse',
  standalone: true,
  imports: [
    CommonModule, RouterModule, FormsModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatChipsModule, MatBadgeModule, MatFormFieldModule, MatInputModule, MatPaginatorModule,
  ],
  templateUrl: './browse.component.html',
})
export class BrowseComponent implements OnInit {
  topics = signal<Topic[]>([]);
  selectedTopicId = signal<string | null>(null);
  vocabPage = signal<Page<Vocabulary> | null>(null);
  searchQuery = signal('');
  loading = signal(false);
  currentPage = signal(0);

  private search$ = new Subject<string>();

  constructor(
    private topicService: TopicService,
    private vocabService: VocabularyService,
  ) {}

  ngOnInit(): void {
    this.topicService.getAll().subscribe(t => {
      this.topics.set(t);
      if (t.length > 0) this.selectTopic(t[0].id);
    });

    this.search$.pipe(
      debounceTime(400),
      switchMap(q => q.trim() ? this.vocabService.search(q, 0, 20) : of(null)),
    ).subscribe(page => {
      if (page) this.vocabPage.set(page);
    });
  }

  selectTopic(id: string): void {
    this.selectedTopicId.set(id);
    this.searchQuery.set('');
    this.currentPage.set(0);
    this.loadVocab();
  }

  onSearch(q: string): void {
    this.searchQuery.set(q);
    this.search$.next(q);
    if (!q) this.loadVocab();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.loadVocab();
  }

  private loadVocab(): void {
    const topicId = this.selectedTopicId();
    if (!topicId) return;
    this.loading.set(true);
    this.vocabService.getByTopic(topicId, this.currentPage(), 20).subscribe({
      next: p => { this.vocabPage.set(p); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  difficultyClass(d: string): string {
    const map: Record<string, string> = {
      EASY: 'bg-green-100 text-green-700',
      MEDIUM: 'bg-yellow-100 text-yellow-700',
      HARD: 'bg-red-100 text-red-700',
    };
    return map[d] ?? 'bg-gray-100 text-gray-600';
  }
}
