import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { LeaderboardService } from '../../core/services/leaderboard.service';
import { ProgressStats } from '../../core/models/leaderboard.model';

interface StatCard {
  label: string;
  value: string | number;
  icon: string;
  color: string;
  bg: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatChipsModule,
  ],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  private leaderboardService = inject(LeaderboardService);

  stats = signal<ProgressStats | null>(null);
  loading = signal(true);
  errorMessage = signal('');

  statCards = signal<StatCard[]>([]);

  masteryColors: Record<string, string> = {
    NEW: 'bg-gray-100 text-gray-700',
    LEARNING: 'bg-blue-100 text-blue-700',
    FAMILIAR: 'bg-yellow-100 text-yellow-700',
    MASTERED: 'bg-green-100 text-green-700',
  };

  ngOnInit(): void {
    this.leaderboardService.getStats().subscribe({
      next: (data) => {
        this.stats.set(data);
        this.loading.set(false);
        this.statCards.set([
          { label: 'Study Sessions', value: data.totalSessions, icon: 'timer', color: 'text-purple-600', bg: 'bg-purple-50' },
          { label: 'Study Minutes', value: data.totalStudyMinutes, icon: 'schedule', color: 'text-blue-600', bg: 'bg-blue-50' },
          { label: 'Words Learned', value: data.totalWordsStudied, icon: 'menu_book', color: 'text-green-600', bg: 'bg-green-50' },
          { label: 'Quizzes Taken', value: data.totalQuizzesCompleted, icon: 'quiz', color: 'text-orange-600', bg: 'bg-orange-50' },
          { label: 'Avg. Quiz Score', value: `${(data.averageQuizScore ?? 0).toFixed(1)}%`, icon: 'star', color: 'text-yellow-600', bg: 'bg-yellow-50' },
        ]);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Failed to load statistics.');
      },
    });
  }

  getMasteryEntries(): { key: string; value: number }[] {
    const breakdown = this.stats()?.masteryBreakdown ?? {};
    return Object.entries(breakdown).map(([key, value]) => ({ key, value }));
  }

  getMasteryTotal(): number {
    return this.getMasteryEntries().reduce((sum, e) => sum + e.value, 0);
  }

  getMasteryPercent(value: number): number {
    const total = this.getMasteryTotal();
    return total > 0 ? Math.round((value / total) * 100) : 0;
  }
}
