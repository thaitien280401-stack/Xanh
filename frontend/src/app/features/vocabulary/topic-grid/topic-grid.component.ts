import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { TopicService } from '../../../core/services/topic.service';
import { Topic, TopicPage, TopicStatus } from '../../../core/models/topic.model';

@Component({
  selector: 'app-topic-grid',
  standalone: true,
  imports: [
    CommonModule, RouterModule,
    MatTabsModule, MatCardModule, MatButtonModule,
    MatIconModule, MatPaginatorModule, MatProgressSpinnerModule, MatChipsModule,
  ],
  templateUrl: './topic-grid.component.html',
  styleUrl: './topic-grid.component.scss',
})
export class TopicGridComponent implements OnInit {
  readonly PAGE_SIZE = 18;

  activeTab   = signal<TopicStatus>('ACTIVE');
  topicPage   = signal<TopicPage | null>(null);
  currentPage = signal(0);
  loading     = signal(false);

  totalElements = computed(() => this.topicPage()?.totalElements ?? 0);
  topics        = computed(() => this.topicPage()?.content ?? []);

  constructor(private topicService: TopicService) {}

  ngOnInit(): void {
    this.loadTopics();
  }

  onTabChange(index: number): void {
    this.activeTab.set(index === 0 ? 'ACTIVE' : 'DONE');
    this.currentPage.set(0);
    this.loadTopics();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage.set(event.pageIndex);
    this.loadTopics();
  }

  trackById(_: number, topic: Topic): string {
    return topic.id;
  }

  private loadTopics(): void {
    this.loading.set(true);
    this.topicService.getPaged(this.activeTab(), this.currentPage(), this.PAGE_SIZE).subscribe({
      next: page => { this.topicPage.set(page); this.loading.set(false); },
      error: ()   => this.loading.set(false),
    });
  }
}
