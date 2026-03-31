import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PomodoroService } from '../../../core/services/pomodoro.service';
import { PomodoroSession } from '../../../core/models/pomodoro.model';

@Component({
  selector: 'app-session-summary',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './session-summary.component.html',
})
export class SessionSummaryComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private pomodoroService = inject(PomodoroService);

  session = signal<PomodoroSession | null>(null);
  loading = signal(true);

  ngOnInit(): void {
    const sessionId = this.route.snapshot.paramMap.get('id')!;
    this.pomodoroService.getSession(sessionId).subscribe({
      next: (s) => {
        this.session.set(s);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  goToQuiz(): void {
    const s = this.session();
    if (s) {
      this.router.navigate(['/quiz'], { queryParams: { topicId: s.topicId } });
    }
  }

  startNewSession(): void {
    this.router.navigate(['/pomodoro']);
  }

  getTimeSpent(): string {
    const s = this.session();
    if (!s) return '—';
    if (!s.endedAt || !s.startedAt) return `${s.durationMinutes} min`;
    const diff = Math.floor(
      (new Date(s.endedAt).getTime() - new Date(s.startedAt).getTime()) / 1000
    );
    const m = Math.floor(diff / 60);
    const sec = diff % 60;
    return `${m}m ${sec}s`;
  }
}
